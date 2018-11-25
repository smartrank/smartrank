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

import java.util.EnumSet;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchJobInfo;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.ScanStatus;

/**
 * Enables or disabled a number of buttons depending on which elemnt in the source list is selected.
 */
public final class ButtonEnablingListSelectionListener implements ListSelectionListener {

    private final JTable _table;
    private final JButton _upButton;
    private final JButton _downButton;
    private final JButton _topButton;
    private final JButton _bottomButton;
    private final JButton _restartButton;
    private final JButton _cancelJobButton;

    /**
     * Constructor.
     *
     * @param table the file table from which the selected item determines the enablement of the buttons
     * @param upButton the Up button is enabled if the table's selected item is not the top item
     * @param downButton the Down button is enabled if the table's selected item is not the bottom item
     * @param topButton the Top button is enabled if the table's selected item is not the top item
     * @param bottomButton the Bottom button is enabled if the table's selected item is not the bottom item
     * @param restartButton the Restart button is enabled if the table's selected item's status is neither Processing nor Pending
     * @param cancelJobButton the Cancel button is enabled if the table's selected item's status is Pending
     */
    public ButtonEnablingListSelectionListener(final JTable table, final JButton upButton, final JButton downButton, final JButton topButton, final JButton bottomButton, final JButton restartButton, final JButton cancelJobButton) {
        _table = table;
        _upButton = upButton;
        _downButton = downButton;
        _topButton = topButton;
        _bottomButton = bottomButton;
        _restartButton = restartButton;
        _cancelJobButton = cancelJobButton;
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            final int selectedRow = _table.getSelectedRow();
            _topButton.setEnabled(selectedRow > 0);
            _upButton.setEnabled(selectedRow > 0);
            _downButton.setEnabled(selectedRow >= 0 && selectedRow < _table.getRowCount() - 1);
            _bottomButton.setEnabled(selectedRow >= 0 && selectedRow < _table.getRowCount() - 1);

            boolean restartEnabled = selectedRow >= 0;
            boolean cancelEnabled = selectedRow >= 0;
            if (restartEnabled) {
                final BatchJobInfo info = (BatchJobInfo) _table.getValueAt(selectedRow, 2);
                restartEnabled = (info.getReader() != null) && !EnumSet.of(ScanStatus.PROCESSING, ScanStatus.PENDING).contains(info.getStatus());
                cancelEnabled = (info.getStatus() == ScanStatus.PENDING);
            }
            _restartButton.setEnabled(restartEnabled);
            _cancelJobButton.setEnabled(cancelEnabled);
        }
    }
}