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
import java.util.HashMap;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * A table that renders rows in alternate colours
 */
public class ZebraTable extends JTable {

    private final Color _evenRowColor;
    private final Color _oddRowColor;

    public void setRowCount(final int i) {
        ((DefaultTableModel) getModel()).setRowCount(i);
    }

    public void addRow(final Object[] data) {
        ((DefaultTableModel) getModel()).addRow(data);
    }

    public Color getOddRowColor() {
        if (isEnabled()) {
            return _oddRowColor;
        }
        return toGrayScale(_oddRowColor);
    }

    public Color getEvenRowColor() {
        if (isEnabled()) {
            return _evenRowColor;
        }
        return toGrayScale(_evenRowColor);
    }

    private Color toGrayScale(final Color color) {
        int component = color.getRed();
        if (color.getBlue() < component) {
            component = color.getBlue();
        }
        if (color.getGreen() < component) {
            component = color.getGreen();
        }
        return new Color(component, component, component);
    }

    /**
     * Private proxy class for the return value of getTableColumn
     */
    private class ZebraTableColumnProxy extends TableColumn {

        private final TableColumn _tableColumnProxy;

        protected ZebraTableColumnProxy(final TableColumn proxy) {
            _tableColumnProxy = proxy;
        }

        @Override
        public void setCellEditor(final TableCellEditor editor) {
            TableCellEditor newEditor = editor;
            if (!(editor instanceof ZebraTableCellEditor)) {
                newEditor = new ZebraTableCellEditor(editor, getName());
            }
            _tableColumnProxy.setCellEditor(newEditor);
        }

        @Override
        public void setCellRenderer(final TableCellRenderer renderer) {
            TableCellRenderer newRenderer = renderer;
            if (!(renderer instanceof ZebraTableCellRenderer)) {
                newRenderer = new ZebraTableCellRenderer(renderer, getName());
            }

            _tableColumnProxy.setCellRenderer(newRenderer);
        }
    }

    public ZebraTable() {
        _evenRowColor = new Color(0xE1, 0xEC, 0xF8);
        _oddRowColor = new Color(0xFE, 0xFE, 0xFE);
        setShowGrid(false);
        setRowSelectionAllowed(true);
        getModel().addTableModelListener(this);
        getTableHeader().setReorderingAllowed(false);
    }

    @Override
    public void setDefaultRenderer(final Class<?> columnClass, final TableCellRenderer renderer) {
        TableCellRenderer newRenderer = renderer;
        if (!(renderer instanceof ZebraTableCellRenderer)) {
            newRenderer = new ZebraTableCellRenderer(renderer, getName());
        }

        super.setDefaultRenderer(columnClass, newRenderer);
    }

    @Override
    public void setDefaultEditor(final Class<?> columnClass, final TableCellEditor editor) {
        TableCellEditor newEditor = editor;
        if (!(editor instanceof ZebraTableCellEditor)) {
            newEditor = new ZebraTableCellEditor(editor, getName());
        }
        super.setDefaultEditor(columnClass, newEditor);
    }

    @Override
    public TableColumn getColumn(final Object identifier) {
        return new ZebraTableColumnProxy(super.getColumn(identifier));
    }

    @Override
    public void setName(final String name) {
        super.setName(name);
        createDefaultEditors();
        createDefaultRenderers();
        final TableCellRenderer headerCellRenderer = getTableHeader().getDefaultRenderer();
        if (!(headerCellRenderer instanceof ZebraTableCellRenderer)) {
            getTableHeader().setDefaultRenderer(new ZebraTableCellRenderer(headerCellRenderer, null));
        }
    }

    @Override
    protected void createDefaultEditors() {
        super.createDefaultEditors();
        final HashMap wrappedEditors = new HashMap();
        for (final Object key : defaultEditorsByColumnClass.keySet()) {
            wrappedEditors.put(key, new ZebraTableCellEditor((TableCellEditor) defaultEditorsByColumnClass.get(key), getName()));
        }
        defaultEditorsByColumnClass.clear();
        defaultEditorsByColumnClass.putAll(wrappedEditors);
    }

    @Override
    protected void createDefaultRenderers() {
        super.createDefaultRenderers();
        final HashMap wrappedRenderers = new HashMap();
        for (final Object key : defaultRenderersByColumnClass.keySet()) {
            wrappedRenderers.put(key, new ZebraTableCellRenderer((TableCellRenderer) defaultRenderersByColumnClass.get(key), getName()));
        }
        defaultRenderersByColumnClass.clear();
        defaultRenderersByColumnClass.putAll(wrappedRenderers);
    }
}
