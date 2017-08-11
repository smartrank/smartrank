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
package nl.minvenj.nfi.smartrank.raven.components.zebra;

import java.awt.Color;
import java.awt.Component;
import java.util.EventObject;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

public class ZebraTableCellEditor implements TableCellEditor {

    private final TableCellEditor _proxyEditor;

    public ZebraTableCellEditor(final TableCellEditor proxy, final String name) {
        _proxyEditor = proxy;
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        final JComponent component = (JComponent) _proxyEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
        final Color backgroundColor = (row % 2) == 0 ? ((ZebraTable) table).getEvenRowColor() : ((ZebraTable) table).getOddRowColor();

        component.setEnabled(table.isEnabled());

        if (component instanceof JSpinner) {
            final JFormattedTextField textField = ((DefaultEditor) ((JSpinner) component).getEditor()).getTextField();
            textField.setBackground(backgroundColor);
            textField.setOpaque(true);
        } else {
            component.setBackground(backgroundColor);
            component.setOpaque(true);
        }
        return component;
    }

    @Override
    public Object getCellEditorValue() {
        return _proxyEditor.getCellEditorValue();
    }

    @Override
    public boolean isCellEditable(final EventObject anEvent) {
        return _proxyEditor.isCellEditable(anEvent);
    }

    @Override
    public boolean shouldSelectCell(final EventObject anEvent) {
        return _proxyEditor.shouldSelectCell(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
        return _proxyEditor.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        _proxyEditor.cancelCellEditing();
    }

    @Override
    public void addCellEditorListener(final CellEditorListener l) {
        _proxyEditor.addCellEditorListener(l);
    }

    @Override
    public void removeCellEditorListener(final CellEditorListener l) {
        _proxyEditor.removeCellEditorListener(l);
    }
}
