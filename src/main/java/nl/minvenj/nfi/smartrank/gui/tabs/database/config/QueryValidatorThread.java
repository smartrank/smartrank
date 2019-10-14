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
package nl.minvenj.nfi.smartrank.gui.tabs.database.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;

/**
 * Performs validation of the configured SQL queries.
 */
final class QueryValidatorThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(QueryValidatorThread.class);
    private static final List<String> VALID_LOCI = Arrays.asList("CSF1PO", "D3S1358", "D5S818", "D7S820", "D8S1179", "D13S317", "D16S539", "D18S51", "D21S11", "FGA", "THO1", "TPOX", "VWA");

    private final DatabaseConfiguration _databaseConfiguration;
    private final DBSettingsDialog _dlg;

    QueryValidatorThread(final DBSettingsDialog dlg, final DatabaseConfiguration databaseConfiguration) {
        _dlg = dlg;
        _databaseConfiguration = databaseConfiguration;
    }

    @Override
    public void run() {

        _dlg.clearErrorMessage();
        try {
            _dlg.setBusy();

            checkQueries(_databaseConfiguration);
            SmartRankGUISettings.setDatabaseType(_dlg.getDatabaseType().toString());
            SmartRankGUISettings.setDatabaseHostPort(_dlg.getHostAndPort());
            SmartRankGUISettings.setDatabaseName(_dlg.getDatabaseName());
            SmartRankGUISettings.setDatabaseUsername(_dlg.getUsername());
            SmartRankGUISettings.setDatabaseSpecimenQuery(_dlg.getSampleQuery());
            SmartRankGUISettings.setDatabaseSpecimenKeysQuery(_dlg.getSampleKeysQuery());
            SmartRankGUISettings.setDatabaseRevisionQuery(_dlg.getDatabaseRevisionQuery());
            SmartRankGUISettings.setDatabaseQuerySpecimenIdColumnIndex(_databaseConfiguration.getSpecimenIdColumnIndex());
            SmartRankGUISettings.setDatabaseQueryLocusColumnIndex(_databaseConfiguration.getLocusColumnIndex());
            SmartRankGUISettings.setDatabaseQueryAlleleColumnIndex(_databaseConfiguration.getAlleleColumnIndex());
            SmartRankGUISettings.setDatabaseSpecimenBatchSize(_dlg.getBatchSize());

            if (_dlg.isSavePassword()) {
                SmartRankGUISettings.setDatabasePassword(new String(_dlg.getPassword()));
            }

            _dlg.setDBSettings(_databaseConfiguration);

            // Dismiss the dialog
            _dlg.setOk();
            _dlg.dispose();
        }
        catch (final Throwable e) {
            LOG.error("Error validating queries!", e);
            _dlg.setErrorMessage("Error validating queries: " + e.getLocalizedMessage());
            _dlg.setIdle();
        }
    }

    private void checkQueries(final DatabaseConfiguration config) throws SQLException {
        if (SmartRankGUISettings.getDatabaseQueriesValidated()) {
            return;
        }

        String currentQuery = "database connection";
        try {
            _dlg.startProgress(5, "Checking database connection");
            LOG.info("Checking database connection to {}", config.getConnectString());
            try (final Connection con = DriverManager.getConnection(config.getConnectString(), config.getUserName(), config.getPassword())) {
                con.setReadOnly(true);
                con.setAutoCommit(false);
                final Statement statement = con.createStatement();

                if (!_dlg.getDatabaseRevisionQuery().isEmpty()) {
                    _dlg.stepProgress("Checking revision query");
                    currentQuery = "Revision query";
                    LOG.info("Checking configured database revision query {}", _dlg.getDatabaseRevisionQuery());
                    final ResultSet resultSetRevisionQuery = statement.executeQuery(_dlg.getDatabaseRevisionQuery());
                    if (!resultSetRevisionQuery.next()) {
                        throw new IllegalArgumentException("Revision query returned no data!");
                    }
                    final ResultSetMetaData revisionMetaData = resultSetRevisionQuery.getMetaData();
                    if (revisionMetaData.getColumnCount() != 1) {
                        throw new IllegalArgumentException("Revision query must return a single column containing the number of valid specimens in the database!");
                    }
                }

                Object firstKey = null;
                Object lastKey = null;
                if (!_dlg.getSampleKeysQuery().isEmpty()) {
                    currentQuery = "Specimen Keys";
                    _dlg.stepProgress("Checking keys query");
                    LOG.info("Checking configured specimen keys query {}", _dlg.getSampleKeysQuery());
                    final ResultSet sampleKeysResultSet = statement.executeQuery(_dlg.getSampleKeysQuery());
                    if (!sampleKeysResultSet.next()) {
                        throw new IllegalArgumentException("Keys query returned no data!");
                    }
                    firstKey = sampleKeysResultSet.getObject(1);
                    int idx = 10;
                    while (idx-- > 0 && sampleKeysResultSet.next()) {
                        lastKey = sampleKeysResultSet.getObject(1);
                    }
                }

                _dlg.stepProgress("Checking specimen query");
                currentQuery = "Specimen query";
                LOG.info("Checking configured specimen query {}", _dlg.getSampleQuery());

                final PreparedStatement ps = con.prepareStatement(_dlg.getSampleQuery());
                if (firstKey != null) {
                    ps.setObject(1, firstKey);
                    ps.setObject(2, lastKey);
                }
                ps.execute();
                final ResultSet resultSetQuery = ps.getResultSet();

                if (!resultSetQuery.next()) {
                    throw new IllegalArgumentException("Specimen query returned no data!");
                }
                final ResultSetMetaData metaData = resultSetQuery.getMetaData();

                _dlg.stepProgress("Checking results");
                LOG.info("Checking specimen results");

                boolean singleRowResult = false;

                // If (some of) the column names contain locus names, we are dealing with a query that returns an entire specimen in one row
                for (int col = 1; col <= metaData.getColumnCount(); col++) {
                    final String colName = metaData.getColumnLabel(col).toUpperCase().replaceAll(" ", "");
                    singleRowResult |= VALID_LOCI.contains(colName);

                    // But we might also be dealing with a query that returns multiple columns for a locus (VWA_1, VWA_2 etc)
                    for (final String valid : VALID_LOCI) {
                        singleRowResult |= colName.startsWith(valid);
                    }
                }

                int idColumn = -1;
                int locusColumn = -1;
                int alleleColumn = -1;
                int keyColumn = -1;
                if (!singleRowResult) {
                    if (metaData.getColumnCount() < 3) {
                        throw new IllegalArgumentException("Expected query that returns at least 3 columns: specimenId, locus and allele. See the manual for more information.");
                    }

                    for (int colIdx = 1; colIdx <= metaData.getColumnCount(); colIdx++) {
                        final String colName = metaData.getColumnLabel(colIdx);
                        if (colName.equalsIgnoreCase("specimenId")) {
                            idColumn = colIdx;
                        }
                        else {
                            if (colName.equalsIgnoreCase("locus")) {
                                locusColumn = colIdx;
                            }
                            else {
                                if (colName.equalsIgnoreCase("allele")) {
                                    alleleColumn = colIdx;
                                }
                                else {
                                    if (keyColumn == -1) {
                                        keyColumn = colIdx;
                                    }
                                }
                            }
                        }
                    }

                    String errorMessage = "";
                    if (idColumn == -1) {
                        errorMessage += " specimenId";
                    }

                    if (locusColumn == -1) {
                        errorMessage += " locus";
                    }

                    if (alleleColumn == -1) {
                        errorMessage += " allele";
                    }

                    if (!errorMessage.isEmpty()) {
                        String msg = errorMessage.trim();
                        final int lastSpaceIndex = msg.lastIndexOf(" ");
                        if (lastSpaceIndex != -1) {
                            msg = "Columns " + msg.substring(0, lastSpaceIndex).replaceAll(" ", ", ") + " and " + msg.substring(lastSpaceIndex + 1) + " are";
                        }
                        else {
                            msg += "Column " + msg + " is";
                        }

                        throw new IllegalArgumentException("Expected a query that returns at least the columns: 'specimenId', 'locus' and 'allele'.\n" + msg + " missing!\nPlease see the manual for more information.");
                    }
                }

                config.setSingleRowQuery(singleRowResult);
                config.setSpecimenIdColumnIndex(idColumn);
                config.setLocusColumnIndex(locusColumn);
                config.setAlleleColumnIndex(alleleColumn);
                SmartRankGUISettings.setDatabaseQueriesValidated(true);
            }
            finally {
                _dlg.stopProgress();
            }
        }
        catch (final Throwable t) {
            throw new IllegalArgumentException(currentQuery + ": " + t.getMessage(), t);
        }
    }
}