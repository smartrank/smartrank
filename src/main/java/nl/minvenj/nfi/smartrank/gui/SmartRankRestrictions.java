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

public class SmartRankRestrictions {


    private static final Logger LOG = LoggerFactory.getLogger(SmartRankRestrictions.class);

    private static final Properties PROPERTIES = new Properties();
    private static final String DEFAULT_PROPERTIES_FILENAME = "SmartRankRestrictions.properties";
    private static final String CASE_LOG_FILENAME = "caseLogFilename";
    private static final String DROPOUT_DEFAULT = "dropoutDefault";
    private static final String DROPOUT_MINIMUM = "dropoutMinimum";
    private static final String DROPOUT_MAXIMUM = "dropoutMaximum";
    private static final String DROPIN_DEFAULT = "dropinDefault";
    private static final String DROPIN_MINIMUM = "dropinMinimum";
    private static final String DROPIN_MAXIMUM = "dropinMaximum";
    private static final String THETA_DEFAULT = "thetaDefault";
    private static final String THETA_MINIMUM = "thetaMinimum";
    private static final String THETA_MAXIMUM = "thetaMaximum";
    private static final String MINIMUM_NUMBER_OF_LOCI = "minimumNumberOfLoci";
    private static final String MAXIMUM_UNKNOWN_COUNT = "maximumUnknownCount";
    private static final String AUTOMATIC_PARAMETER_ESTIMATION_ENABLED = "automaticParameterEstimationEnabled";
    private static final String MANUAL_PARAMETER_ESTIMATION_ENABLED = "interactiveParameterEstimationEnabled";
    private static final String PARAMETER_ESTIMATION_ITERATIONS = "parameterEstimationIterations";
    private static final String PARAMETER_ESTIMATION_DROPOUT_PERCENTILE = "parameterEstimationDropoutPercentile";
    private static final String REPORT_TEMPLATE_FILENAME = "reportTemplateFilename";
    private static final String REPORT_FILENAME = "reportFilename";
    private static final String MAXIMUM_STORED_RESULTS = "maximumStoredResults";
    private static final String MAXIMUM_PATH_LENGTH = "maximumPathLength";
    private static final String SHOW_OPTIMIZATIONS_MENU = "showOptimizationsMenu";
    private static final String ALL_LRS_STORED = "allLRsStored";
    private static final String OUTPUT_ROOT_FOLDER = "outputRootFolder";
    private static final String LR_THRESHOLD = "defaultLRThreshold";
    private static final String EXPORT_MATCHING_PROFILES_AFTER_SEARCH = "exportMatchingProfilesAfterSearch";
    private static final String BATCH_MODE = "batchMode";
    private static final String Q_SHUTDOWN = "qDesignationShutdown";
    private static final String IS_WINDOW_CLOSE_BLOCKED_IN_BATCH_MODE = "windowCloseBlockedInBatchMode";
    private static final String BATCH_AUTOSTART_MODE = "batchMode.autoStart";

    private static String _propertiesFileName = System.getProperty("smartrankRestrictions");

    private static long _loadedDate;

    public static int getMaximumUnknownCount() {
        return getInt(MAXIMUM_UNKNOWN_COUNT, 4);
    }

    public static int getParameterEstimationIterations() {
        return getInt(PARAMETER_ESTIMATION_ITERATIONS, 10000);
    }

    public static double getDropoutDefault() {
        return getDouble(DROPOUT_DEFAULT, 0.03);
    }

    public static double getDropoutMinimum() {
        return getDouble(DROPOUT_MINIMUM, 0);
    }

    public static double getDropoutMaximum() {
        return getDouble(DROPOUT_MAXIMUM, 0.99);
    }

    public static double getThetaDefault() {
        return getDouble(THETA_DEFAULT, 0.01);
    }

    public static double getThetaMinimum() {
        return getDouble(THETA_MINIMUM, 0);
    }

    public static double getThetaMaximum() {
        return getDouble(THETA_MAXIMUM, 0.5);
    }

    public static double getDropinDefault() {
        return getDouble(DROPIN_DEFAULT, 0.05);
    }

    public static double getDropinMinimum() {
        return getDouble(DROPIN_MINIMUM, 0);
    }

    public static double getDropinMaximum() {
        return getDouble(DROPIN_MAXIMUM, 0.5);
    }

    public static int getMinimumNumberOfLoci() {
        return getInt(MINIMUM_NUMBER_OF_LOCI, 1);
    }

    public static int getMaximumStoredResults() {
        return getInt(MAXIMUM_STORED_RESULTS, 250);
    }

    public static boolean isOptimizationsMenuShown() {
        return Boolean.parseBoolean(get(SHOW_OPTIMIZATIONS_MENU, "false"));
    }

    public static boolean isAllLRsStored() {
        return Boolean.parseBoolean(get(ALL_LRS_STORED, "false"));
    }

    public static boolean isAutomaticParameterEstimationEnabled() {
        return Boolean.parseBoolean(get(AUTOMATIC_PARAMETER_ESTIMATION_ENABLED, "false"));
    }

    public static boolean isManualParameterEstimationEnabled() {
        return Boolean.parseBoolean(get(MANUAL_PARAMETER_ESTIMATION_ENABLED, "true"));
    }

    public static boolean isProfileExportEnabled() {
        return Boolean.parseBoolean(get(EXPORT_MATCHING_PROFILES_AFTER_SEARCH, "true"));
    }

    public static String getReportTemplateFilename() {
        return get(REPORT_TEMPLATE_FILENAME, "report/SmartRank.jrxml");
    }

    public static String getReportFilename() {
        return get(REPORT_FILENAME, "{CASEFILES_FOLDER}/SmartRankResults/SmartRank-{CRIMESCENE_PROFILES}-{DATE}-{TIME}.pdf");
    }

    public static String getCaseLogFilename() {
        return get(CASE_LOG_FILENAME, "{CASEFILES_FOLDER}/SmartRankResults/SmartRank-{CRIMESCENE_PROFILES}-{DATE}-{TIME}.log");
    }

    public static String getOutputRootFolder() {
        return get(OUTPUT_ROOT_FOLDER, "");
    }

    public static boolean isBatchMode() {
        return Boolean.parseBoolean(get(BATCH_MODE, "false"));
    }

    public static Integer getDefaultLRThreshold() {
        return getInt(LR_THRESHOLD, 1000);
    }

    public static int getDropoutEstimationPercentile() {
        return getInt(PARAMETER_ESTIMATION_DROPOUT_PERCENTILE, 95);
    }

    public static int getMaximumPathLength() {
        return getInt(MAXIMUM_PATH_LENGTH, 128);
    }

    public static String getBatchAutoStartMode() {
        return get(BATCH_AUTOSTART_MODE, "");
    }

    public static boolean isQDesignationShutdownForHp() {
        return Boolean.parseBoolean(get(Q_SHUTDOWN, "true"));
    }

    public static boolean isWindowCloseBlockedInBatchMode() {
        return Boolean.parseBoolean(get(IS_WINDOW_CLOSE_BLOCKED_IN_BATCH_MODE, "false"));
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
        PROPERTIES.setProperty(key, value);
        store();
    }

    private static void load() {
        if ((System.currentTimeMillis() - _loadedDate) > 2000L) {
            if (_propertiesFileName == null) {
                if (!load(DEFAULT_PROPERTIES_FILENAME)) {
                    load(System.getProperty("user.home") + File.separatorChar + DEFAULT_PROPERTIES_FILENAME);
                }
            }
            else {
                load(_propertiesFileName);
            }
            _loadedDate = System.currentTimeMillis();
        }
    }

    private static boolean load(final String fileName) {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            if (PROPERTIES.isEmpty()) {
                LOG.info("Loading restrictions from {}", new File(fileName).getAbsolutePath());
            }
            PROPERTIES.load(fis);
            _propertiesFileName = fileName;
            return true;
        }
        catch (final FileNotFoundException ex) {
            LOG.debug("Properties file {} does not exist yet.", new File(fileName).getAbsolutePath());
        }
        catch (final Exception ex) {
            LOG.debug("Error loading properties file: \n" + ex.getLocalizedMessage());
        }
        return false;
    }

    private static void store() {
        if (_propertiesFileName == null) {
            if (!store(DEFAULT_PROPERTIES_FILENAME)) {
                store(System.getProperty("user.home") + File.separatorChar + DEFAULT_PROPERTIES_FILENAME);
            }
        }
        else {
            store(_propertiesFileName);
        }
    }

    private static boolean store(final String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            PROPERTIES.store(fos, "Created by " + System.getProperty("user.name") + " using SmartRank " + SmartRank.getRevision());
            _propertiesFileName = fileName;
            return true;
        }
        catch (final Exception ex) {
            LOG.debug("Error writing properties file: \n" + ex.getLocalizedMessage());
        }
        return false;
    }

    private static double getDouble(final String key, final double defaultValue) {
        try {
            return Double.parseDouble(get(key, "" + defaultValue));
        }
        catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    private static int getInt(final String key, final int defaultValue) {
        try {
            final int parsedInt = Integer.parseInt(get(key, "" + defaultValue));
            if (parsedInt <= 0) {
                LOG.warn("Value for {} ({}) <= 0. Returning default ({}) instead.", key, parsedInt, defaultValue);
                return defaultValue;
            }
            return parsedInt;
        }
        catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    private SmartRankRestrictions() {
    }

}
