/*
 * Copyright (C) 2016 Netherlands Forensic Institute
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

import java.sql.SQLException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class JDBCSampleIterator implements Iterator<Sample> {

    private static final Logger LOG = LoggerFactory.getLogger(JDBCSampleIterator.class);

    private JDBCResultSetChunker _resultSet;
    private final JDBCReader _reader;
    private Sample _nextSample;
    private long _lastAccessTime;

    public JDBCSampleIterator(final JDBCReader reader) {
        _reader = reader;
        _lastAccessTime = System.currentTimeMillis();

        final Thread watchDog = new Thread() {

            @Override
            public void run() {
                int timeout = 300000;
                while (!isInterrupted() && (System.currentTimeMillis() - _lastAccessTime < timeout)) {
                    try {
                        Thread.sleep(timeout);
                    }
                    catch (final InterruptedException e) {
                    }
                    timeout = 30000;
                }
                if (_nextSample != null) {
                    LOG.info("Shutting down JDBC reader due to timeout");
                    _reader.close();
                }
            }
        };

        watchDog.start();
    }

    @Override
    public boolean hasNext() {
        _lastAccessTime = System.currentTimeMillis();
        if (_nextSample == null) {
            try {
                _nextSample = _reader.readSample(getResultSet());
                _lastAccessTime = System.currentTimeMillis();
            }
            catch (final SQLException e) {
                LOG.error("Error accessing the database", e);
                MessageBus.getInstance().send(this, new ErrorStringMessage("Error accessing the database!\n" + e.getMessage()));
                throw new RuntimeException(e);
            }
        }
        return (_nextSample != null);
    }

    private JDBCResultSetChunker getResultSet() throws SQLException {
        if (_resultSet == null) {
            _resultSet = _reader.getResultSet();
            _lastAccessTime = System.currentTimeMillis();
        }
        return _resultSet;
    }

    @Override
    public Sample next() {
        _lastAccessTime = System.currentTimeMillis();
        final Sample s = _nextSample;
        _nextSample = null;
        return s;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported.");
    }

}
