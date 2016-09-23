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
package nl.minvenj.nfi.smartrank.gui.tabs.search.hypothesis;

import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;

public class DropoutProbabilityCellEditor extends AbstractCellEditor implements TableCellEditor {
    private static final Logger LOG = LoggerFactory.getLogger(DropoutProbabilityCellEditor.class);
    private final JSpinner _spinner = new JSpinner();

    public DropoutProbabilityCellEditor() {
        _spinner.setBorder(null);
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        LOG.debug("getTableCellEditorComponent {}/{} value {}", row, column, value);
        _spinner.setModel(new SpinnerNumberModel((Double) value, new Double(SmartRankRestrictions.getDropoutMinimum()), new Double(SmartRankRestrictions.getDropoutMaximum()), new Double(0.01)));
        _spinner.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        table.setValueAt(_spinner.getValue(), row, column);
                    }
                });
            }
        });
        return _spinner;
    }

    @Override
    public Object getCellEditorValue() {
        LOG.debug("getCellEditorValue returning {}", _spinner.getValue());
        return _spinner.getValue();
    }
}
