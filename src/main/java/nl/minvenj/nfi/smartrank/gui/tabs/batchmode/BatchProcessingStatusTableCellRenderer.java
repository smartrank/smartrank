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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

final class BatchProcessingStatusTableCellRenderer implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        final ScanStatus status = ((BatchJobInfo) value).getStatus();

        final JLabel component = new JLabel(status.toString(), SwingConstants.CENTER);
        switch (status) {
            case PENDING:
                break;
            case PROCESSING:
                component.setBackground(adaptToSelection(isSelected, Color.BLUE));
                component.setForeground(Color.WHITE);
                component.setOpaque(true);
                break;
            case CANCELLED:
                component.setBackground(adaptToSelection(isSelected, Color.BLACK));
                component.setForeground(Color.WHITE);
                component.setOpaque(true);
                break;
            case REMOVED:
                component.setBackground(adaptToSelection(isSelected, Color.GRAY));
                component.setForeground(Color.WHITE);
                component.setOpaque(true);
                break;
            case INTERRUPTED:
                component.setBackground(adaptToSelection(isSelected, Color.YELLOW));
                component.setForeground(Color.BLACK);
                component.setOpaque(true);
                break;
            case FAILED:
                component.setBackground(adaptToSelection(isSelected, Color.RED));
                component.setForeground(Color.WHITE);
                component.setOpaque(true);
                break;
            case SUCCEEDED:
                component.setBackground(adaptToSelection(isSelected, Color.GREEN));
                component.setForeground(Color.BLACK);
                component.setOpaque(true);
                break;
            case POST_PROCESSING_SCRIPT_ERROR:
                component.setBackground(adaptToSelection(isSelected, Color.PINK));
                component.setForeground(Color.BLACK);
                component.setOpaque(true);
                break;
            default:
                break;
        }
        return component;
    }

    private Color adaptToSelection(final boolean isSelected, final Color color) {
        return isSelected ? color.darker() : color;
    }
}