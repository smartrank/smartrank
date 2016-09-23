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
package nl.minvenj.nfi.smartrank.gui.status;

import java.awt.AWTException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.SmartRank;
import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.gui.TimeUpdater;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;
import nl.minvenj.nfi.smartrank.messages.commands.StopCurrentOperationCommand;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.messages.status.DetailStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.PercentReadyMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchCompletedMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.annotations.ExecuteOnSwingEventThread;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.raven.timeformat.TimeUtils;

public class StatusPanel extends SmartRankPanel {

    private static final Logger LOG = LoggerFactory.getLogger(SmartRankPanel.class);

    private final TrayIconHandler _trayIconHandler;
    private String _detailMessage;
    private final JProgressBar _progressBar;
    private final JSeparator _separatorLeft;
    private final JSeparator _separatorRight;
    private final JLabel _statusMessageLabel;
    private final JLabel _stopButton;
    private final JLabel _timeRunningLabel;
    private final JLabel _timeLeftLabel;
    private final JLabel _timeRunningHeader;
    private final JLabel _timeLeftHeader;

    /**
     * Creates new StatusPanel
     */
    public StatusPanel() {
        this.setName("StatusPanel");
        _detailMessage = "";
        _trayIconHandler = new TrayIconHandler(this);

        setLayout(new MigLayout("ins 0 5 2 5", "[64px,grow,fill][5px][64px][64px][64px][49.00px][2px][40%][16px]", "[17px:17px:17px,shrink 0]"));

        _statusMessageLabel = new JLabel();
        add(_statusMessageLabel, "cell 0 0,alignx left,growy");

        _separatorLeft = new JSeparator(SwingConstants.VERTICAL);
        add(_separatorLeft, "cell 1 0,grow");

        _timeRunningHeader = new JLabel("Running:", SwingConstants.TRAILING);
        add(_timeRunningHeader, "cell 2 0,growx,aligny top");

        _timeRunningLabel = new JLabel("", SwingConstants.LEADING);
        add(_timeRunningLabel, "cell 3 0,grow");

        _timeLeftHeader = new JLabel("Remaining:", SwingConstants.TRAILING);
        add(_timeLeftHeader, "cell 4 0,growx,aligny top");

        _timeLeftLabel = new JLabel("", SwingConstants.LEADING);
        add(_timeLeftLabel, "cell 5 0,grow");

        _separatorRight = new JSeparator(SwingConstants.VERTICAL);
        add(_separatorRight, "cell 6 0,alignx left,growy");

        _progressBar = new JProgressBar(0, 100);
        _progressBar.setStringPainted(true);
        add(_progressBar, "cell 7 0,growx,aligny top");

        _stopButton = new JLabel(new ImageIcon(getClass().getResource("/images/16x16/molumen-red-square-error-warning-icon.png")));
        _stopButton.setToolTipText("Click here to abort the current task");
        _stopButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(final java.awt.event.MouseEvent evt) {
                stopButtonMouseClicked(evt);
            }
        });
        add(_stopButton, "cell 8 0,alignx left,aligny top");

        try {
            _trayIconHandler.register();
        }
        catch (final AWTException ex) {
        }

        _statusMessageLabel.setName("statusMessage");
        _timeRunningLabel.setName("timeRunning");
        _timeRunningHeader.setLabelFor(_timeRunningLabel);
        _timeLeftLabel.setName("timeRemaining");
        _timeLeftHeader.setLabelFor(_timeLeftLabel);
        _progressBar.setName("taskProgressBar");
        _stopButton.setName("stopButton");

        registerAsListener();
    }

    private void stopButtonMouseClicked(final java.awt.event.MouseEvent evt) {
        _stopButton.setEnabled(false);
        _stopButton.repaint();
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(SwingUtilities.windowForComponent(this), "Abort the current operation?", "SmartRank " + SmartRank.getVersion(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
            MessageBus.getInstance().send(this, new StopCurrentOperationCommand());
        }
        _stopButton.setEnabled(true);
    }

    @RavenMessageHandler(ApplicationStatusMessage.class)
    @ExecuteOnSwingEventThread
    void onStatusChange(final ApplicationStatus status) {
        _progressBar.setValue(0);
        _progressBar.setVisible(status.isActive());
        _progressBar.setString(null);
        _stopButton.setVisible(status.isInterruptable());
        _separatorRight.setVisible(status.isActive());
        _separatorLeft.setVisible(status.isActive());
        _statusMessageLabel.setText(status.getMessage());
        _timeRunningHeader.setVisible(status.isActive());
        _timeLeftHeader.setVisible(status.isActive());
        _timeRunningLabel.setText("");
        _timeLeftLabel.setText("");
        if (status.isActive()) {
            TimeUpdater.getInstance().addLabels(_timeRunningLabel, _timeLeftLabel);
        }
        else {
            TimeUpdater.getInstance().interrupt();
        }
        _trayIconHandler.setStatus(status);
    }

    @RavenMessageHandler(PercentReadyMessage.class)
    @ExecuteOnSwingEventThread
    void onPercentReady(final int percent) {
        _progressBar.setValue(percent);
        if (_detailMessage != null && !_detailMessage.isEmpty())
            _progressBar.setString(_detailMessage + " (" + percent + "%)");
        TimeUpdater.getInstance().setPercentReady(percent);
        _trayIconHandler.setPercentReady(percent);
    }

    @RavenMessageHandler(ErrorStringMessage.class)
    @ExecuteOnSwingEventThread
    void onErrorMessageChanged(final String message) {
        LOG.error("Displaying error message: {}", message);
        JOptionPane.showMessageDialog(getParent(), message, "SmartRank error", JOptionPane.ERROR_MESSAGE);
    }

    @RavenMessageHandler(SearchCompletedMessage.class)
    void onSearchCompleted(final SearchResults results) {
        final String message = String.format("<html>Search completed.<br>  Number of specimens evaluated: <b>%d</b><br>  Number of LRs > 1: <b>%d</b><br>  Number of excluded profiles: <b>%d</b><br>  Duration: <b>%s</b>",
                                             results.getNumberOfLRs(),
                                             results.getNumberOfLRsOver1(),
                                             results.getExcludedProfiles().size(),
                                             TimeUtils.formatDuration(results.getDuration()));
        JOptionPane.showMessageDialog(getParent(), message, "SmartRank Search Completed", JOptionPane.INFORMATION_MESSAGE);
    }

    @RavenMessageHandler(DetailStringMessage.class)
    @ExecuteOnSwingEventThread
    void onDetailMessage(final String detailMessage) {
        _detailMessage = detailMessage;
        if (_detailMessage == null || _detailMessage.isEmpty()) {
            _progressBar.setString(null);
        }
        else {
            _progressBar.setString(detailMessage + " (" + _progressBar.getValue() + "%)");
        }
        _trayIconHandler.setDetailMessage(detailMessage);
    }
}
