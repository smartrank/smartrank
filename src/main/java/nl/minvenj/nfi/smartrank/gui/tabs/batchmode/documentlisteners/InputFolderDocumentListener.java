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

/**
 * Listens for changes to the input folder name and updates the processing, failed and succeeded folder references.
 */
public final class InputFolderDocumentListener implements DocumentListener {

    private final BatchModePanel _batchModePanel;

    /**
     * Constructor.
     *
     * @param batchModePanel the parent panel
     */
    public InputFolderDocumentListener(final BatchModePanel batchModePanel) {
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
        synchronized (_batchModePanel._messageBus) {
            if (_batchModePanel.getFilesTable() != null) {
                _batchModePanel.getFilesTable().setRowCount(0);
            }
            _batchModePanel.setInputFolder(new File(_batchModePanel._inputFolderField.getText()));
            if (_batchModePanel.getInputFolder().exists() && _batchModePanel.getInputFolder().isDirectory()) {
                SmartRankGUISettings.setLastSelectedSearchCriteriaPath(_batchModePanel.getInputFolder().getAbsolutePath());
            }

            _batchModePanel._processingFolder = new File(_batchModePanel.getInputFolder(), "processing");
            _batchModePanel.setFailedFolder(new File(_batchModePanel.getInputFolder(), "failed"));
            _batchModePanel._succeededFolder = new File(_batchModePanel.getInputFolder(), "succeeded");
        }
    }
}