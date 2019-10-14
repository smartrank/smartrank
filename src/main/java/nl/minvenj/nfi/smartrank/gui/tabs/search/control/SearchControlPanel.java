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
package nl.minvenj.nfi.smartrank.gui.tabs.search.control;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.analysis.SearchResultCache;
import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.gui.TimeUpdater;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;
import nl.minvenj.nfi.smartrank.messages.commands.StartAnalysisCommand;
import nl.minvenj.nfi.smartrank.messages.data.DefenseHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.ProsecutionHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.SearchResultsMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.PercentReadyMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchAbortedMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchCompletedMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.annotations.ExecuteOnSwingEventThread;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.report.jasper.ReportGenerator;

public class SearchControlPanel extends SmartRankPanel {

    private static final Logger LOG = LoggerFactory.getLogger(SearchControlPanel.class);

    private final JLabel _runningTime;
    private final JLabel _runningTimeLabel;
    private final JButton _showReportButton;
    private final JButton _startButton;
    private final JLabel _timeRemaining;
    private final JLabel _timeRemainingLabel;
    private final JButton _showLogButton;
    private SearchResults _lastResult;
    private final SearchResultCache _resultCache;
    private final MessageBus _messageBus;
    private final JButton _exportButton;

    /**
     * Creates new form AnalysisControlPanel
     */
    public SearchControlPanel() {
        _messageBus = MessageBus.getInstance();
        _resultCache = SearchResultCache.getInstance();
        _startButton = new javax.swing.JButton();
        _runningTimeLabel = new javax.swing.JLabel();
        _timeRemainingLabel = new javax.swing.JLabel();
        _runningTime = new javax.swing.JLabel();
        _timeRemaining = new javax.swing.JLabel();
        _showReportButton = new javax.swing.JButton();

        _startButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/magnifier.png"))); // NOI18N
        _startButton.setText("Search");
        _startButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        _runningTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        _runningTimeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/time.png"))); // NOI18N
        _runningTimeLabel.setText("Running Time:");

        _timeRemainingLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        _timeRemainingLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/time_go.png"))); // NOI18N
        _timeRemainingLabel.setText("Est. Time Remaining:");

        _runningTime.setFont(new java.awt.Font("Courier New", 1, 24)); // NOI18N
        _runningTime.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        _runningTime.setText("00:00:00");
        _runningTime.setOpaque(true);

        _timeRemaining.setFont(new java.awt.Font("Courier New", 1, 24)); // NOI18N
        _timeRemaining.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        _timeRemaining.setText("00:00:00");
        _timeRemaining.setOpaque(true);

        _showReportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/report_go.png"))); // NOI18N
        _showReportButton.setText("Show report");
        _showReportButton.setEnabled(false);
        _showReportButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                showReportButtonActionPerformed(evt);
            }
        });

        _showLogButton = new JButton("Show Log");
        _showLogButton.setEnabled(false);
        _showLogButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/text_align_justify.png"))); // NOI18N
        _showLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                showLogButtonActionPerformed(e);
            }
        });

        setLayout(new MigLayout("", "[430px]", "[25px][][25px][][18px][28px][18px][28px]"));
        add(_startButton, "cell 0 0,growx,aligny top");

        _exportButton = new JButton("Export Search Criteria");
        _exportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/disk.png"))); // NOI18N
        _exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                exportSearchCriteria();
            }
        });
        add(_exportButton, "cell 0 1,growx,aligny top");
        add(_showReportButton, "cell 0 2,growx,aligny top");
        add(_showLogButton, "cell 0 3,grow");
        add(_runningTime, "cell 0 5,alignx right,aligny top");
        add(_runningTimeLabel, "cell 0 4,alignx right,growy");
        add(_timeRemainingLabel, "cell 0 6,alignx right,growy");
        add(_timeRemaining, "cell 0 7,alignx right,aligny top");

        _startButton.setName("searchButton");
        _showReportButton.setName("showReportButton");
        _showLogButton.setName("showLogButton");
        _runningTime.setName("runningTime");
        _runningTimeLabel.setLabelFor(_runningTime);
        _timeRemaining.setName("timeRemaining");
        _timeRemainingLabel.setLabelFor(_timeRemaining);

        registerAsListener();
    }

    private void exportSearchCriteria() {
        final SearchCriteriaExportDialog dlg = new SearchCriteriaExportDialog();
        dlg.setModal(true);
        dlg.setVisible(true);
    }

    private void startButtonActionPerformed(final java.awt.event.ActionEvent evt) {
        // If Hd has no contributors, warn the user that this might not be what they want.
        final DefenseHypothesis defenseHypothesis = _messageBus.query(DefenseHypothesisMessage.class);
        if (defenseHypothesis.getContributors().isEmpty() && defenseHypothesis.getUnknownCount() == 0 &&
            JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(getParent().getParent(),
                                                                   "<html>The defense hypothesis has no contributors!<br>Are you sure this is what you intended?",
                                                                   "SmartRank Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
            return;
        }

        // If the prosecution and defense hypotheses have an unequal number of contributors, warn the user.
        final int defenseDonors = defenseHypothesis.getContributors().size() + defenseHypothesis.getUnknownCount();
        final ProsecutionHypothesis prosecutionHypothesis = _messageBus.query(ProsecutionHypothesisMessage.class);
        final int prosecutionDonors = prosecutionHypothesis.getContributors().size() + prosecutionHypothesis.getUnknownCount();
        if (defenseDonors != prosecutionDonors &&
            JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(getParent().getParent(),
                                                                   String.format("<html>The prosecution hypothesis specifies <b>%d</b> contributors, but the defense hypothesis specifies <b>%d</b>.<br>Are you sure this is what you intended?", prosecutionDonors, defenseDonors),
                                                                   "SmartRank Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
            return;
        }
        _lastResult = null;
        _messageBus.send(this, new StartAnalysisCommand());
    }

    private void showLogButtonActionPerformed(final ActionEvent e) {
        if (Desktop.isDesktopSupported()) {
            if (_lastResult != null && _lastResult.getLogFileName() != null) {
                File file = new File(_lastResult.getLogFileName());

                // Hold down control to open the file location instead of the file itself
                if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
                    file = file.getParentFile();
                }

                try {
                    Desktop.getDesktop().open(file);
                }
                catch (final Throwable t) {
                    LOG.error("Error showing logfile!", t);
                    _messageBus.send(this, new ErrorStringMessage("Error showing logfile '" + _lastResult.getLogFileName() + "'!\n" + t.getMessage()));
                }
            }
            else {
                LOG.error("ShowLog button clicked but no seach logfile available! SearchResults = {}", _lastResult);
            }
        }
    }

    @RavenMessageHandler(SearchCompletedMessage.class)
    private void onSearchComplete(final SearchResults results) {
        _lastResult = results;
        _resultCache.put(results);
        _showReportButton.setEnabled(true);
        _showLogButton.setEnabled(true);
    }

    @RavenMessageHandler(SearchAbortedMessage.class)
    private void onSearchAborted(final SearchResults results) {
        _lastResult = results;
        _showReportButton.setEnabled(false);
        _showLogButton.setEnabled(true);

        final Throwable failureReason = results.getFailureReason();
        LOG.error("Error running analysis!", failureReason);
        if (failureReason instanceof java.lang.OutOfMemoryError) {
            final String errorMessage = String.format("<html><b>SmartRank has run out of memory!</b><br>Apparently %d MB was not enough.<br>%sFor further information on how to prevent this problem, see the <i>Troubleshooting</i> section in the manual.",
                                                      Runtime.getRuntime().maxMemory() / (1024 * 1024),
                                                      (failureReason.getMessage() != null ? "The error message was: <i>" + failureReason.getMessage() + "</i><br>" : ""));
            _messageBus.send(this, new ErrorStringMessage(errorMessage));
        }
        else {
            if (failureReason instanceof java.lang.InterruptedException) {
                _messageBus.send(this, new ErrorStringMessage("Search interrupted!"));
            }
            else {
                _messageBus.send(this, new ErrorStringMessage("<html><b>Search failed!</b><br>Please check the logfile for details.<br>The message was:<br>  <i>" + failureReason.getLocalizedMessage() + "</i>"));
            }
        }
    }

    private void showReportButtonActionPerformed(final java.awt.event.ActionEvent evt) {
        if (Desktop.isDesktopSupported()) {
            if (_lastResult != null) {
                String fileName = _lastResult.getReportFileName();
                try {
                    if (fileName == null || !new File(fileName).exists()) {
                        fileName = new ReportGenerator().generateAndWait(_lastResult.getStartTime());
                        _lastResult.setReportName(fileName);
                    }

                    File file = new File(fileName);

                    // Hold down control to open the file location instead of the file itself
                    if ((evt.getModifiers() & InputEvent.CTRL_MASK) != 0) {
                        file = file.getParentFile();
                    }

                    Desktop.getDesktop().open(file);
                }
                catch (final Throwable t) {
                    LOG.error("Error showing report!", t);
                    _messageBus.send(this, new ErrorStringMessage("Error showing report '" + fileName + "'!\n" + t.getMessage()));
                }
            }
            else {
                LOG.error("ShowReport button clicked but no seach report available!");
            }
        }
        _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.READY_FOR_ANALYSIS));
    }

    @RavenMessageHandler(ApplicationStatusMessage.class)
    @ExecuteOnSwingEventThread
    public void onStatusChange(final ApplicationStatus status) {
        _startButton.setEnabled(status == ApplicationStatus.READY_FOR_ANALYSIS);
        enableReportAndLogButtons(status);
        _runningTimeLabel.setVisible(status.isActive());
        _timeRemainingLabel.setVisible(status.isActive());
        _runningTime.setVisible(status.isActive());
        _timeRemaining.setVisible(status.isActive());
        if (status.isActive()) {
            LOG.debug("{}", status);
            TimeUpdater.getInstance().addLabels(_runningTime, _timeRemaining);
        }
        else {
            TimeUpdater.getInstance().interrupt();
        }
    }

    private void enableReportAndLogButtons(final ApplicationStatus status) {
        _showReportButton.setEnabled(!status.isActive() && _lastResult != null && _lastResult.isSucceeded());
        _showLogButton.setEnabled(!status.isActive() && _lastResult != null);
    }

    @RavenMessageHandler(PercentReadyMessage.class)
    public void onProgress(final int percent) {
        TimeUpdater.getInstance().setPercentReady(percent);
    }

    @RavenMessageHandler({ProsecutionHypothesisMessage.class, DefenseHypothesisMessage.class})
    @ExecuteOnSwingEventThread
    private void clearResults() {
        final DefenseHypothesis hd = _messageBus.query(DefenseHypothesisMessage.class);
        final ProsecutionHypothesis hp = _messageBus.query(ProsecutionHypothesisMessage.class);
        _lastResult = _resultCache.get(hp, hd);
        enableReportAndLogButtons(_messageBus.query(ApplicationStatusMessage.class));
        _messageBus.send(this, new SearchResultsMessage(_lastResult));
    }
}
