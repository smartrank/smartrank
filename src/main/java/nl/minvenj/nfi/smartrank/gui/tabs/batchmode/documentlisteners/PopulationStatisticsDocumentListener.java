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
package nl.minvenj.nfi.smartrank.gui.tabs.batchmode.documentlisteners;

import java.io.File;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchModePanel;
import nl.minvenj.nfi.smartrank.io.statistics.StatisticsReader;

/**
 * Listens for changes to the population statistics filename and update the last selected filename so the file will be used in the search.
 */
public final class PopulationStatisticsDocumentListener implements DocumentListener {

    private final BatchModePanel _batchModePanel;

    /**
     * Constructor.
     *
     * @param batchModePanel the parent panel
     */
    public PopulationStatisticsDocumentListener(final BatchModePanel batchModePanel) {
        _batchModePanel = batchModePanel;
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
        doUpdate();
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
        doUpdate();
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
        doUpdate();
    }

    private void doUpdate() {
        boolean enableRun = true;
        _batchModePanel._statisticsErrorLabel.setText("");
        _batchModePanel._statisticsErrorLabel.setVisible(false);
        if (!_batchModePanel._popStatsFilenameField.getText().isEmpty()) {
            try {
                new StatisticsReader(new File(_batchModePanel._popStatsFilenameField.getText())).getStatistics();
                SmartRankGUISettings.setLastSelectedStatisticsFileName(_batchModePanel._popStatsFilenameField.getText());
            }
            catch (final Throwable t) {
                String msg = t.getClass().getSimpleName().replaceAll("([a-z])([A-Z])", "$1 $2").replaceAll(" Exception", "");
                if (t.getLocalizedMessage() != null && !t.getLocalizedMessage().isEmpty()) {
                    msg += ": " + t.getLocalizedMessage();
                }
                _batchModePanel._statisticsErrorLabel.setText(msg);
                _batchModePanel._statisticsErrorLabel.setVisible(true);
                enableRun = false;
            }
        }
        else {
            SmartRankGUISettings.setLastSelectedStatisticsFileName(_batchModePanel._popStatsFilenameField.getText());
        }

        if (_batchModePanel._runButton != null) {
            _batchModePanel._runButton.setEnabled(enableRun);
        }
    }
}