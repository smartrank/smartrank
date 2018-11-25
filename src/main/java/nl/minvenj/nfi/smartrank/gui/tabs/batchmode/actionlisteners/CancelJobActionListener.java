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
package nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchJobInfo;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchModePanel;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.ScanStatus;

/**
 *  Cancels the currently selected job.
 */
public final class CancelJobActionListener implements ActionListener {

    private final BatchModePanel _batchModePanel;

    /**
     * Constructor.
     *
     * @param batchModePanel the parent panel
     */
    public CancelJobActionListener(final BatchModePanel batchModePanel) {
        _batchModePanel = batchModePanel;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final int selectedRow = _batchModePanel.getFilesTable().getSelectedRow();
        final File file = (File) _batchModePanel.getFilesTable().getValueAt(selectedRow, 0);
        _batchModePanel.getFilesTable().setValueAt(_batchModePanel.moveFileToDatedFolder(file, _batchModePanel.getFailedFolder()), selectedRow, 0);

        final BatchJobInfo jobInfo = (BatchJobInfo) _batchModePanel.getFilesTable().getValueAt(selectedRow, 1);
        jobInfo.setStatus(ScanStatus.CANCELLED);

        _batchModePanel.getFilesTable().setValueAt(jobInfo, selectedRow, 1);
        _batchModePanel.getFilesTable().setValueAt(jobInfo, selectedRow, 2);
    }
}