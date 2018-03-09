/*
 * Copyright (C) 2016 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.gui.tabs.batchmode;

import javax.swing.table.DefaultTableModel;

public class BatchProcessingTableModel extends DefaultTableModel {

    private final BatchProcessingTable _table;

    public BatchProcessingTableModel(final BatchProcessingTable batchProcessingTable) {
        super(new String[]{"File", "Status", "Info"}, 0);
        _table = batchProcessingTable;
    }

    @Override
    public boolean isCellEditable(final int row, final int column) {
        return false;
    }

    @Override
    public void setValueAt(final Object aValue, final int row, final int column) {
        super.setValueAt(aValue, row, column);
        if(_table.isRowSelected(row))
            _table.valueChanged(null);
    }
}
