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
package nl.minvenj.nfi.smartrank.gui.status;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import nl.minvenj.nfi.smartrank.SmartRank;
import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchCompletedMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.raven.timeformat.TimeUtils;

public class TrayIconHandler {

    private TrayIcon _trayIcon;
    private final Image _idleIcon;
    private final Image _busyIcon;
    private final String _versionString;
    private final Component _parent;
    private final ArrayList<Image> _busyIcons;
    private String _statusMessage;
    private String _detailMessage;

    public TrayIconHandler(final Component parent) {
        _trayIcon = null;
        _parent = parent;
        _versionString = "SmartRank v" + SmartRank.getVersion();
        _idleIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/tray/icon-16.png"));
        _busyIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/tray/icon-16-busy.png"));
        _busyIcons = new ArrayList<>();
        int idx = 1;
        URL imageUrl;
        while ((imageUrl = getClass().getResource("/images/tray/icon-16-busy-" + idx + ".png")) != null) {
            _busyIcons.add(Toolkit.getDefaultToolkit().getImage(imageUrl));
            idx++;
        }

        if (SystemTray.isSupported()) {
            _trayIcon = new TrayIcon(_idleIcon, _versionString);
            _trayIcon.setImageAutoSize(true);
            _trayIcon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    final JFrame parentFrame = (JFrame) SwingUtilities.windowForComponent(_parent);
                    parentFrame.setState(JFrame.NORMAL);
                    parentFrame.toFront();
                }
            });
        }
    }

    @RavenMessageHandler(ErrorStringMessage.class)
    public void onUpdateErrorMessage(final String message) {
        final JFrame parentFrame = (JFrame) SwingUtilities.windowForComponent(_parent);
        if (!parentFrame.isActive()) {
            if (_trayIcon != null) {
                _trayIcon.displayMessage(_versionString, removeHtml(message), TrayIcon.MessageType.ERROR);
            }
        }
    }

    @RavenMessageHandler(SearchCompletedMessage.class)
    void onSearchCompleted(final SearchResults results) {
        final JFrame parentFrame = (JFrame) SwingUtilities.windowForComponent(_parent);
        if (!parentFrame.isActive()) {
            if (_trayIcon != null) {
                final String message = String.format(
                                                     "<html>Search completed.<br>  Number of specimens evaluated: <b>%d</b><br>  Number of LRs > 1: <b>%d</b><br>  Duration: <b>%s</b>",
                                                     results.getNumberOfLRs(),
                                                     results.getNumberOfLRsOver1(),
                                                     TimeUtils.formatDuration(results.getDuration()));
                _trayIcon.displayMessage(_versionString, removeHtml(message), TrayIcon.MessageType.INFO);
            }
        }
    }

    /**
     * Removes any HTML/XML tags from the supplied string.
     */
    private String removeHtml(final String string) {
        return string.replaceAll("\\<\\s*[bB][rR]\\s*/?\\s*\\>", "\n").replaceAll("\\<[^\\>]+\\>", "");
    }

    public void register() throws AWTException {
        if (SystemTray.isSupported()) {
            SystemTray.getSystemTray().add(_trayIcon);
            MessageBus.getInstance().registerSubscriber(this);
        }
    }

    void setStatus(final ApplicationStatus status) {
        if (SystemTray.isSupported()) {
            _trayIcon.setImage(status.isActive() ? _busyIcon : _idleIcon);
            _statusMessage = status.getMessage();
            _detailMessage = "";
            _trayIcon.setToolTip(_versionString + "\n" + _statusMessage);
        }
    }

    void setDetailMessage(final String detailMessage) {
        if (SystemTray.isSupported()) {
            _detailMessage = detailMessage;
            _trayIcon.setToolTip(_versionString + "\n" + _statusMessage + "\n" + _detailMessage);
        }
    }

    void setPercentReady(final int percent) {
        if (SystemTray.isSupported()) {
            final int iconIndex = (percent * (_busyIcons.size() - 1)) / 100;
            _trayIcon.setImage(_busyIcons.get(iconIndex));
            _trayIcon.setToolTip(_versionString + "\n" + _statusMessage + "\n" + ((_detailMessage == null || _detailMessage.isEmpty()) ? "" : _detailMessage + "\n") + (percent + "% done"));
        }
    }
}
