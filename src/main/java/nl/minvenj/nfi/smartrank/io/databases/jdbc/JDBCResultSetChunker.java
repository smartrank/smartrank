/*
 * Copyright (C) 2017 Netherlands Forensic Institute
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.minvenj.nfi.smartrank.io.databases.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.messages.status.DetailStringMessage;
import nl.minvenj.nfi.smartrank.raven.NullUtils;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class JDBCResultSetChunker implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(JDBCResultSetChunker.class);
    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(2);

    private List<Object> _ids;
    private ResultSet _resultSet;
    private int _curPos;

    private final Connection _connection;
    private final DatabaseConfiguration _config;

    private long _numberOfSpecimens;
    private String _contentHash;
    private final BlockingQueue<Future<ResultSet>> _futures;

    private class ResultSetFetcher implements Callable<ResultSet> {
        private final Object _fromKey;
        private final Object _toKey;

        public ResultSetFetcher(final Object fromKey, final Object toKey) {
            _fromKey = fromKey;
            _toKey = toKey;
        }

        @Override
        public ResultSet call() throws Exception {
            LOG.info("Executing query with ID between {} and {}", _fromKey, _toKey);
            final Connection conn = DriverManager.getConnection(_config.getConnectString(), _config.getProperties());
            final PreparedStatement statement = conn.prepareStatement(SmartRankGUISettings.getDatabaseSpecimenQuery());
            statement.setObject(1, _fromKey);
            statement.setObject(2, _toKey);
            final ResultSet resultSet = statement.executeQuery();
            LOG.info("Query executed.");
            return resultSet;
        }
    }

    public JDBCResultSetChunker(final Connection connection, final DatabaseConfiguration config) throws SQLException {
        _connection = connection;
        _config = config;
        _futures = new ArrayBlockingQueue<>(2);
        _curPos = 0;

        initQueries();
        requestNextBatch();
        requestNextBatch();
    }

    private void requestNextBatch() {
        if (_curPos < _ids.size()) {
            final Object fromId = _ids.get(_curPos);

            if (SmartRankGUISettings.getDatabaseSpecimenBatchSize() == 0) {
                _curPos = _ids.size() - 1;
            }
            else {
                _curPos = Math.min(_curPos + SmartRankGUISettings.getDatabaseSpecimenBatchSize() - 1, _ids.size() - 1);
            }

            final Object toId = _ids.get(_curPos);
            _curPos++;

            _futures.add(SERVICE.submit(new ResultSetFetcher(fromId, toId)));
        }
    }

    public boolean next() throws SQLException {
        updateResultSet(false);
        return _resultSet.next();
    }

    private void updateResultSet(final boolean doNext) throws SQLException {
        if (_resultSet != null && (_resultSet.isClosed() || !_resultSet.isAfterLast())) {
            return;
        }

        if (_resultSet != null) {
            _resultSet.getStatement().getConnection().close();
        }

        final Future<ResultSet> future = _futures.poll();
        if (future == null) {
            return;
        }

        try {
            _resultSet = future.get();
        }
        catch (final Exception e) {
            throw new SQLException("Error getting result set!", e);
        }

        if (doNext) {
            _resultSet.next();
        }

        requestNextBatch();
    }

    private void initQueries() throws SQLException {
        // If we have a query that obtains the list of keys, perform that query now.
        if (!NullUtils.getValue(_config.getSpecimenKeyQuery(), "").trim().isEmpty()) {
            LOG.info("Getting specimen IDs using query: {}", _config.getSpecimenKeyQuery());
            MessageBus.getInstance().send(this, new DetailStringMessage("Getting Specimen keys"));
            try (Statement st = _connection.createStatement()) {
                final ResultSet idSet = st.executeQuery(_config.getSpecimenKeyQuery());

                _ids = new ArrayList<>();
                while (idSet.next()) {
                    _ids.add(idSet.getObject(1));
                }

                _numberOfSpecimens = _ids.size();
            }
            LOG.info("Got {} specimen IDs", _numberOfSpecimens);
        }

        _contentHash = "";
        if (!NullUtils.getValue(_config.getDatabaseRevisionQuery(), "").trim().isEmpty()) {
            try (Statement statement = _connection.createStatement()) {
                final ResultSet resultSet = statement.executeQuery(_config.getDatabaseRevisionQuery());
                if (!resultSet.next()) {
                    LOG.error("Could not obtain database revision!");
                    _contentHash = "could not be determined";
                }
                else {
                    _contentHash = resultSet.getObject(1).toString();
                }
            }
            catch (final SQLException e) {
                LOG.error("Error getting database revision!", e);
            }
        }
    }

    public long getNumberOfSpecimens() {
        return _numberOfSpecimens;
    }

    public String getContentHash() {
        return _contentHash;
    }

    @Override
    public void close() {
        if (_resultSet != null) {
            try {
                _resultSet.getStatement().getConnection().close();
                _resultSet.getStatement().close();
                _resultSet.close();
            }
            catch (final SQLException e) {
                // Don't care...
            }
        }
    }

    public String getString(final int columnIndex) throws SQLException {
        final String string = _resultSet.getString(columnIndex);
        return string;
    }

    public boolean isAfterLast() throws SQLException {
        try {
            updateResultSet(true);
            return _resultSet.isClosed() || _resultSet.isAfterLast();
        }
        catch (final Throwable t) {
            return true;
//            throw new SQLException("Error in JDBCResultSetChunker!", t);
        }
    }

    public boolean isBeforeFirst() throws SQLException {
        return _resultSet == null || _resultSet.isBeforeFirst();
    }

    public SQLWarning getWarnings() throws SQLException {
        if (_resultSet == null) {
            return null;
        }
        return _resultSet.getWarnings();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        if (_resultSet == null) {
            return null;
        }
        return _resultSet.getMetaData();
    }
}
