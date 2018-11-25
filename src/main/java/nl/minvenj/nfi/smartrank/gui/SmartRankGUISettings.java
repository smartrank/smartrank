/*
 * Copyright (C) 2015 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.SmartRank;
import nl.minvenj.nfi.smartrank.raven.NullUtils;

public class SmartRankGUISettings {

    private static final String DATABASE_QUERY_BATCH_SIZE = "jdbc.query.batchSize";

    private static final Logger LOG = LoggerFactory.getLogger(SmartRankGUISettings.class);

    private static final Properties PROPERTIES = new Properties();
    private static final String DEFAULT_PROPERTIES_FILENAME = "SmartRank.properties";
    private static final String LAST_SELECTED_DATABASE_FILENAME = "lastSelectedDatabasePath";
    private static final String LAST_SELECTED_CRIMESCENE_PATH = "lastSelectedCrimescenePath";
    private static final String LAST_SELECTED_KNOWNPROFILE_PATH = "lastSelectedKnownProfilePath";
    private static final String LAST_SELECTED_STATISTICS_FILENAME = "lastSelectedStatisticsPath";
    private static final String LAST_SELECTED_SEARCH_CRITERIA_PATH = "lastSelectedSearchCriteriaPath";
    private static final String DATABASE_TYPE = "jdbc.databaseType";
    private static final String DATABASE_HOSTPORT = "jdbc.hostPort";
    private static final String DATABASE_SCHEMANAME = "jdbc.schemaName";
    private static final String DATABASE_PASSWORD = "jdbc.userName";
    private static final String DATABASE_USERNAME = "jdbc.password";
    private static final String DATABASE_QUERY_SPECIMENS = "jdbc.query.specimens";
    private static final String DATABASE_QUERY_SPECIMENKEYS = "jdbc.query.specimenkeys";
    private static final String DATABASE_QUERY_REVISION = "jdbc.query.revision";
    private static final String DATABASE_QUERIES_VALIDATED = "jdbc.queries.validated";
    private static final String DATABASE_QUERY_SPECIMENS_SINGLEROW = "jdbc.query.specimens.singleRow";
    private static final String DATABASE_QUERY_SPECIMENID_COLUMN_INDEX = "jdbc.query.columns.specimenId";
    private static final String DATABASE_QUERY_LOCUS_COLUMN_INDEX = "jdbc.query.columns.locus";
    private static final String DATABASE_QUERY_ALLELE_COLUMN_INDEX = "jdbc.query.columns.allele";
    private static final String BATCHMODE_START_TIME = "batchmode.startTime";
    private static final String BATCHMODE_END_TIME = "batchmode.endTime";
    private static final String WINDOW_TITLE = "windowTitle";

    private static String _propertiesFileName = System.getProperty("smartrankProperties");

    public static String getLastSelectedDatabaseFileName() {
        return get(LAST_SELECTED_DATABASE_FILENAME, "");
    }

    public static void setLastSelectedDatabaseFileName(final String connectString) {
        set(LAST_SELECTED_DATABASE_FILENAME, connectString);
    }

    public static String getLastSelectedCrimescenePath() {
        return get(LAST_SELECTED_CRIMESCENE_PATH, "");
    }

    public static void setLastSelectedCrimescenePath(final String lastSelectedPath) {
        set(LAST_SELECTED_CRIMESCENE_PATH, lastSelectedPath);
    }

    public static String getLastSelectedKnownProfilePath() {
        return get(LAST_SELECTED_KNOWNPROFILE_PATH, "");
    }

    public static void setLastSelectedKnownProfilePath(final String lastSelectedPath) {
        set(LAST_SELECTED_KNOWNPROFILE_PATH, lastSelectedPath);
    }

    public static String getLastSelectedStatisticsFileName() {
        return get(LAST_SELECTED_STATISTICS_FILENAME, "");
    }

    public static void setLastSelectedStatisticsFileName(final String lastSelectedPath) {
        set(LAST_SELECTED_STATISTICS_FILENAME, lastSelectedPath);
    }

    public static String getDatabaseType() {
        return get(DATABASE_TYPE, "SQLServer");
    }

    public static void setDatabaseType(final String type) {
        set(DATABASE_TYPE, type);
    }

    public static String getDatabaseHostPort() {
        return get(DATABASE_HOSTPORT, "");
    }

    public static void setDatabaseHostPort(final String hostPort) {
        set(DATABASE_HOSTPORT, hostPort);
    }

    public static String getDatabaseSchemaName() {
        return get(DATABASE_SCHEMANAME, "");
    }

    public static void setDatabaseName(final String name) {
        set(DATABASE_SCHEMANAME, name);
    }

    public static String getDatabaseUsername() {
        return get(DATABASE_USERNAME, "");
    }

    public static void setDatabaseUsername(final String name) {
        set(DATABASE_USERNAME, name);
    }

    public static String getDatabasePassword() {
        return get(DATABASE_PASSWORD, "");
    }

    public static void setDatabasePassword(final String password) {
        set(DATABASE_PASSWORD, password);
    }

    public static String getDatabaseSpecimenQuery() {
        return get(DATABASE_QUERY_SPECIMENS, "");
    }

    public static void setDatabaseSpecimenQuery(final String query) {
        set(DATABASE_QUERY_SPECIMENS, query);
    }

    public static String getDatabaseSpecimenKeysQuery() {
        return get(DATABASE_QUERY_SPECIMENKEYS, "");
    }

    public static void setDatabaseSpecimenKeysQuery(final String query) {
        set(DATABASE_QUERY_SPECIMENKEYS, query);
    }

    public static String getDatabaseRevisionQuery() {
        return get(DATABASE_QUERY_REVISION, "");
    }

    public static void setDatabaseRevisionQuery(final String query) {
        set(DATABASE_QUERY_REVISION, query);
    }

    public static void setDatabaseQueriesValidated(final boolean validated) {
        set(DATABASE_QUERIES_VALIDATED, Boolean.toString(validated));
    }

    public static boolean getDatabaseQueriesValidated() {
        return Boolean.valueOf(get(DATABASE_QUERIES_VALIDATED, "false"));
    }

    public static void setDatabaseQuerySpecimenIdColumnIndex(final int specimenIdColumnIndex) {
        set(DATABASE_QUERY_SPECIMENID_COLUMN_INDEX, "" + specimenIdColumnIndex);
    }

    public static int getDatabaseQuerySpecimenIdColumnIndex() {
        return Integer.decode(get(DATABASE_QUERY_SPECIMENID_COLUMN_INDEX, "-1"));
    }

    public static void setDatabaseQueryLocusColumnIndex(final int locusColumnIndex) {
        set(DATABASE_QUERY_LOCUS_COLUMN_INDEX, "" + locusColumnIndex);
    }

    public static int getDatabaseQueryLocusColumnIndex() {
        return Integer.decode(get(DATABASE_QUERY_LOCUS_COLUMN_INDEX, "-1"));
    }

    public static void setDatabaseQueryAlleleColumnIndex(final int alleleColumnIndex) {
        set(DATABASE_QUERY_ALLELE_COLUMN_INDEX, "" + alleleColumnIndex);
    }

    public static int getDatabaseQueryAlleleColumnIndex() {
        return Integer.decode(get(DATABASE_QUERY_ALLELE_COLUMN_INDEX, "-1"));
    }

    public static void setDatabaseSpecimenQuerySingleRow(final boolean singleRowQuery) {
        set(DATABASE_QUERY_SPECIMENS_SINGLEROW, Boolean.toString(singleRowQuery));
    }

    public static boolean isSingleRowSpecimenQuery() {
        return Boolean.valueOf(get(DATABASE_QUERY_SPECIMENS_SINGLEROW, "false"));
    }

    public static String getLastSelectedSearchCriteriaPath() {
        return get(LAST_SELECTED_SEARCH_CRITERIA_PATH, "");
    }

    public static void setLastSelectedSearchCriteriaPath(final String path) {
        set(LAST_SELECTED_SEARCH_CRITERIA_PATH, path);
    }

    public static String getBatchModeStartTime() {
        return get(BATCHMODE_START_TIME, "00:00");
    }

    public static void setBatchModeStartTime(final String time) {
        set(BATCHMODE_START_TIME, time);
    }

    public static String getBatchModeEndTime() {
        return get(BATCHMODE_END_TIME, "23:59");
    }

    public static void setBatchModeEndTime(final String time) {
        set(BATCHMODE_END_TIME, time);
    }

    public static int getDatabaseSpecimenBatchSize() {
        return Integer.decode(get(DATABASE_QUERY_BATCH_SIZE, "2000"));
    }

    public static void setDatabaseSpecimenBatchSize(final int batchSize) {
        set(DATABASE_QUERY_BATCH_SIZE, Integer.toString(batchSize));
    }

    public static String getWindowTitle() {
        return get(WINDOW_TITLE, "");
    }

    private static String get(final String key, final String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            load();
            value = PROPERTIES.getProperty(key);
            if (value == null) {
                set(key, defaultValue);
                value = defaultValue;
            }
        }
        return value;
    }

    private static void set(final String key, final String value) {
        load();
        if (!NullUtils.getValue(value, "").equals(PROPERTIES.getProperty(key))) {
        PROPERTIES.setProperty(key, value);
        store();
    }
    }

    private static void load() {
        if (_propertiesFileName == null) {
            if (!load(DEFAULT_PROPERTIES_FILENAME))
                load(System.getProperty("user.home") + File.separatorChar + DEFAULT_PROPERTIES_FILENAME);
        }
        else {
            load(_propertiesFileName);
        }
    }

    private static boolean load(final String fileName) {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            PROPERTIES.load(fis);
            if (_propertiesFileName == null) {
                LOG.info("Loaded GUI properties from {}", fileName);
            }
            _propertiesFileName = fileName;
            return true;
        }
        catch (final FileNotFoundException ex) {
            LOG.debug("Properties file {} does not exist yet.", fileName);
        }
        catch (final Exception ex) {
            LOG.debug("Error loading properties file: \n" + ex.getLocalizedMessage());
        }
        return false;
    }

    private static void store() {
        if (_propertiesFileName == null) {
            if (!store(DEFAULT_PROPERTIES_FILENAME))
                store(System.getProperty("user.home") + File.separatorChar + DEFAULT_PROPERTIES_FILENAME);
        }
        else {
            store(_propertiesFileName);
        }
    }

    private static boolean store(final String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            PROPERTIES.store(fos, "Created by SmartRank " + SmartRank.getRevision());
            _propertiesFileName = fileName;
            return true;
        }
        catch (final Exception ex) {
            LOG.debug("Error writing properties file: \n" + ex.getLocalizedMessage());
        }
        return false;
    }
}
