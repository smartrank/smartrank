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

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ZebraTableCellRenderer implements TableCellRenderer {

    private final TableCellRenderer _proxyRenderer;
    private boolean _overrideOpacity;

    public ZebraTableCellRenderer(final TableCellRenderer proxy) {
        this(proxy, false);
    }

    public ZebraTableCellRenderer(final TableCellRenderer proxy, final boolean overrideOpacity) {
        _proxyRenderer = proxy;
        _overrideOpacity = overrideOpacity;
    }

    public ZebraTableCellRenderer(final TableCellEditor proxy) {
        _proxyRenderer = null;
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        final JComponent component = (JComponent) _proxyRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color backgroundColor = (row % 2) == 0 ? ((ZebraTable) table).getEvenRowColor() : ((ZebraTable) table).getOddRowColor();

        component.setEnabled(table.isEnabled());

        if (row != -1 && (_overrideOpacity || !component.isOpaque())) {
            component.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            if (isSelected && table.isEnabled()) {
                backgroundColor = table.getSelectionBackground();
            }

            if (component instanceof JSpinner) {
                final JFormattedTextField textField = ((JSpinner.DefaultEditor) ((JSpinner) component).getEditor()).getTextField();
                textField.setBackground(backgroundColor);
                textField.setOpaque(true);
            }
            else {
                component.setBackground(backgroundColor);
                component.setOpaque(true);
            }
        }
        return component;
    }
}
