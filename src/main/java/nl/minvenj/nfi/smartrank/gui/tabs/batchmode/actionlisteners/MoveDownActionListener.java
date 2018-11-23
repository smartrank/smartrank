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

import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchModePanel;

/**
 * Moves the currently selected item in the files table one position up.
 */
public final class MoveDownActionListener implements ActionListener {

    private final BatchModePanel _batchModePanel;

    /**
     * Constructor.
     *
     * @param batchModePanel the parent panel
     */
    public MoveDownActionListener(final BatchModePanel batchModePanel) {
        _batchModePanel = batchModePanel;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final int selectedRow = _batchModePanel.getFilesTable().getSelectedRow();
        _batchModePanel.getFilesTable().moveRow(selectedRow, selectedRow + 1);
    }
}