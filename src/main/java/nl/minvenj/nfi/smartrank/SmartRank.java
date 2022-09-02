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
package nl.minvenj.nfi.smartrank;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.util.StatusPrinter;
import nl.minvenj.nfi.smartrank.analysis.CaseLogger;
import nl.minvenj.nfi.smartrank.cli.SmartRankCLI;

public class SmartRank {

    private static Logger _log;

    public static void main(final String[] args) {
        try {
            final String logConfigLocation = setupLogging();
            _log = LoggerFactory.getLogger(SmartRank.class);

            _log.info("Starting SmartRank Version {}", getRevision());
            _log.info("Max memory: {} bytes, {} MB", Runtime.getRuntime().maxMemory(), Runtime.getRuntime().maxMemory() / 1048576);
            _log.info("Log configuration read from {}", logConfigLocation);
            _log.info("Case logging is {}", CaseLogger.isEnabled()?"enabled":"disabled");
            final ArrayList<String> javaProperties = new ArrayList<>();
            final Enumeration<?> propertyNames = System.getProperties().propertyNames();
            while (propertyNames.hasMoreElements()) {
                final String name = "" + propertyNames.nextElement();
                if (name.startsWith("java.")) {
                    final String value = System.getProperty(name);
                    javaProperties.add("  " + name + " = " + value);
                }
            }
            Collections.sort(javaProperties);
            for (final String line : javaProperties) {
                _log.info(line);
            }

            SmartRankManager.getInstance();

            _log.info("Starting SmartRank GUI");
            startGUI();

            try {
                SmartRankCLI.process(args);
            }
            catch (final IOException ex) {
                _log.error("Invalid options", ex);
            }
        }
        catch (final Throwable t) {
            _log.error("Error starting SmartRank!", t);
        }
    }

    private static String setupLogging() {
        // By default, logging configuration is read from here.
        String configLocation = "logback defaults";
        final File configFile = new File(System.getProperty("loggingConfigurationFile", "./logback.xml"));

        if (configFile.exists() && configFile.canRead()) {
            // assume SLF4J is bound to logback in the current environment
            final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

            final JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            // Call context.reset() to clear any previous configuration, e.g. default configuration
            context.reset();
            try {
                // try to configure using the logback.xml in the application directory
                configurator.doConfigure(configFile);
                configLocation = configFile.getAbsolutePath();
            }
            catch (final JoranException je) {
                if (!GraphicsEnvironment.isHeadless()) {
                    final StringBuilder sb = new StringBuilder();
                    for (final Status status : context.getStatusManager().getCopyOfStatusList()) {
                        if (status.getLevel() == Status.ERROR) {
                            StatusPrinter.buildStr(sb, "", status);
                        }
                    }
                    String message = sb.toString();
                    final int end = message.indexOf("\n");
                    if (end >= 0) {
                        message = message.substring(0, end);
                    }
                    message = message.replaceAll("-", "<br>");
                    message = message.replaceAll("\\s([A-Z][a-z])", "<br>$1");
                    JOptionPane.showMessageDialog(null,
                                                  "<html>Logging configuration corrupt!<br>File: <b>" + configFile.getAbsolutePath() + "</b><br>Message: <i>" + je.getMessage() + "</i><br><code>" + message);
                }
            }
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        }
        return configLocation;
    }

    private static void startGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            _log.error("Error setting look-and-feel", ex);
        }

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new nl.minvenj.nfi.smartrank.gui.SmartRankGUI().setVisible(true);
                }
                catch (final Throwable t) {
                    _log.error("Error starting SmartRank!", t);
                    System.exit(-1);
                }
            }
        });
    }

    public static String getVersion() {
        final String version = SmartRank.class.getPackage().getSpecificationVersion();
        if (version == null) {
            return "DEBUG";
        }
        return version;
    }

    public static String getRevision() {
        String revision = SmartRank.class.getPackage().getImplementationVersion();
        if (revision == null) {
            revision = "Unknown Revision";
        }
        return getVersion() + ", revision " + revision;
    }

    public static String getSignatureInfo() {
        String signerInfo = "This version is not signed";
        if (SmartRank.class.getSigners() != null) {
            signerInfo = "<html>This version is digitally signed by the following entities:<UL>";
            for (final Object signer : SmartRank.class.getSigners()) {
                if (signer instanceof X509Certificate) {
                    final X509Certificate cert = (X509Certificate) signer;
                    String dn = cert.getSubjectDN().getName();
                    String ca = cert.getIssuerDN().getName();
                    dn = dn.substring(dn.indexOf("CN=") + 3);
                    dn = dn.substring(0, dn.indexOf(", "));
                    ca = ca.substring(ca.indexOf("CN=") + 3);
                    ca = ca.substring(0, ca.indexOf(", "));
                    signerInfo += "<LI>" + dn + " (certified by " + ca + ")" + "</LI>";
                }
                else {
                    signerInfo += "<LI>Unknown certificate type " + signer.getClass().getName() + "</LI>";
                }
            }
            signerInfo += "</UL>";
        }
        return signerInfo;
    }
}
