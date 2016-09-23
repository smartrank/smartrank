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

import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ZebraTableCellRenderer implements TableCellRenderer {

    private final TableCellRenderer _proxyRenderer;
    private final String _name;

    public ZebraTableCellRenderer(final TableCellRenderer proxy, final String name) {
        _proxyRenderer = proxy;
        _name = name;
    }

    public ZebraTableCellRenderer(final TableCellEditor proxy, final String name) {
        _proxyRenderer = null;
        _name = name;
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        final Component component = _proxyRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color evenRowColor = Color.WHITE;
        Color oddRowColor = Color.WHITE;
        if (table instanceof ZebraTable) {
            evenRowColor = ((ZebraTable) table).getEvenRowColor();
            oddRowColor = ((ZebraTable) table).getOddRowColor();
        }

        if (isSelected) {
            evenRowColor = table.getSelectionBackground();
            oddRowColor = evenRowColor;
        }

        component.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        component.setEnabled(table.isEnabled());

        if (_name != null) {
            if (component instanceof JSpinner) {
                ((JSpinner.DefaultEditor) ((JSpinner) component).getEditor()).getTextField().setBackground((row % 2) == 0 ? evenRowColor : oddRowColor);
            } else {
                component.setBackground((row % 2) == 0 ? evenRowColor : oddRowColor);
            }
        }
        return component;
    }
}
