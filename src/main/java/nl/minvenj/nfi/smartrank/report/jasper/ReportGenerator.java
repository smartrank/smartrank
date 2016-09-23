/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.smartrank.report.jasper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.JRExpressionEvalException;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.io.WritableFileSource;
import nl.minvenj.nfi.smartrank.messages.commands.WritableFileSourceMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.messages.status.DetailStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.PercentReadyMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.report.jasper.api.JasperDataSource;
import nl.minvenj.nfi.smartrank.utils.DomainExpressionResolver;
import nl.minvenj.nfi.smartrank.utils.OutputLocationResolver;

public class ReportGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ReportGenerator.class);

    private class GeneratorThread extends Thread {

        private MessageBus _messageBus;
        private String _reportFilename;
        private final long _dateTime;

        public GeneratorThread(final long dateTime) {
            _dateTime = dateTime;
        }

        @Override
        public void run() {

            _messageBus = MessageBus.getInstance();

            try {
                _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.SAVING_REPORT));
                _messageBus.send(this, new PercentReadyMessage(0));
                _messageBus.send(this, new DetailStringMessage("Loading report definition"));
                LOG.debug("Attempting to load report definition");
                final InputStream reportStream = new FileInputStream(SmartRankRestrictions.getReportTemplateFilename());
                final Map<String, Object> parameters = new HashMap<>();

                _messageBus.send(this, new PercentReadyMessage(25));
                _messageBus.send(this, new DetailStringMessage("Compiling report definition"));

                LOG.debug("Compiling report");
                final JasperReport jReport = JasperCompileManager.compileReport(reportStream);

                _messageBus.send(this, new PercentReadyMessage(50));
                _messageBus.send(this, new DetailStringMessage("Filling report"));
                LOG.debug("Filling report");
                final JasperPrint print = JasperFillManager.fillReport(jReport, parameters, new JasperDataSource());

                // Always export to pdf, regardless of what the user does in the dialog
                _messageBus.send(this, new PercentReadyMessage(75));
                _messageBus.send(this, new DetailStringMessage("Saving report file"));

                _reportFilename = DomainExpressionResolver.resolve(SmartRankRestrictions.getReportFilename(), _dateTime);
                if (!_reportFilename.matches(".*\\.[pP][dD][fF]$")) {
                    _reportFilename += ".pdf";
                }

                // If an output location was supplied, it overrides the path of the output filename, but not the name of the file itself.
                _reportFilename = OutputLocationResolver.resolve(_reportFilename);

                final WritableFileSource writableFileSource = _messageBus.query(WritableFileSourceMessage.class);
                _reportFilename = writableFileSource.getWritableFile(_reportFilename);
                if (_reportFilename.isEmpty()) {
                    LOG.error("Could not select a writable file!");
                    throw new IllegalArgumentException(_reportFilename + " is not writable and no writable alternative was selected!");
                }

                LOG.debug("Saving report to {}", _reportFilename);
                JasperExportManager.exportReportToPdfFile(print, _reportFilename);

                _messageBus.send(this, new PercentReadyMessage(99));
                _messageBus.send(this, new DetailStringMessage(""));
            }
            catch (final JRExpressionEvalException ex) {
                LOG.error("Error evaluation expression in report template '{}'", SmartRankRestrictions.getReportTemplateFilename(), ex);
                final String concatenatedMessage = concatenateMessages(ex);
                _messageBus.send(this, new ErrorStringMessage("<html>The report definition file contains an error.<br><i>" + concatenatedMessage + "</i><br>See the logfile for details."));
            }
            catch (final JRException ex) {
                LOG.error("Error building report from template '{}'", SmartRankRestrictions.getReportTemplateFilename(), ex);
                _messageBus.send(this, new ErrorStringMessage("<html>An error occurred building the report:<br>" + concatenateMessages(ex)));
            }
            catch (final FileNotFoundException fnfe) {
                LOG.error("The report file '{}' could not be found", SmartRankRestrictions.getReportTemplateFilename(), fnfe);
                _messageBus.send(this, new ErrorStringMessage("<html>The report file could not be found:<br>" + fnfe.getMessage()));
            }
            catch (final Throwable t) {
                LOG.error("There was an error creating report from template '{}':", SmartRankRestrictions.getReportTemplateFilename(), t);
                _messageBus.send(this, new ErrorStringMessage("<html>There was an error building the report:<br>" + t.getClass().getName() + "<br>" + t.getMessage() + "<br>Check the logfile for details."));
            }
        }

        private String concatenateMessages(final Throwable t) {
            final StringBuilder concatenatedMessages = new StringBuilder(safeQuote(t.getMessage()));
            Throwable current = t;
            Throwable cause = t.getCause();
            while (cause != null && cause != current) {
                current = cause;
                cause = cause.getCause();
                concatenatedMessages.append("<br>").append(safeQuote(current.getMessage()));
            }
            return concatenatedMessages.toString();
        }

        private String safeQuote(final String rawString) {
            return rawString.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("%", "%%").replaceAll("\n", "<br>");
        }
    }

    public void generateAsync(final long dateTime) {
        new GeneratorThread(dateTime).start();
    }

    public String generateAndWait(final long dateTime) throws InterruptedException {
        final GeneratorThread generatorThread = new GeneratorThread(dateTime);
        generatorThread.start();
        generatorThread.join();
        return generatorThread._reportFilename;
    }
}
