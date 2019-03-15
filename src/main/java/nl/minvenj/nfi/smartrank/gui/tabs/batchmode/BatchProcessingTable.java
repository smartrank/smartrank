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

import java.awt.Component;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableCellRenderer;

import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;
import nl.minvenj.nfi.smartrank.raven.NullUtils;
import nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable;

public class BatchProcessingTable extends ZebraTable {

    private final BatchModeDetailPanel _detailPanel;

    public BatchProcessingTable(final String name, final BatchModeDetailPanel detailPanel) {
        _detailPanel = detailPanel;

        setModel(new BatchProcessingTableModel(this));
        setName(name);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        getColumn("File").setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                return new JLabel(((File) value).getName());
            }
        });
        getColumn("Status").setCellRenderer(new BatchProcessingStatusTableCellRenderer());
        getColumn("Info").setCellRenderer(new BatchProcessingInfoTableCellRenderer());
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (e != null)
            super.valueChanged(e);
        if (e == null || !e.getValueIsAdjusting()) {
            _detailPanel.clear();
            final int selectedRow = getSelectedRow();
            if (selectedRow >= 0) {
                final File searchCriteriaFile = (File) getValueAt(selectedRow, 0);
                _detailPanel.setCriteriaFileName(searchCriteriaFile.getAbsolutePath());

                final BatchJobInfo info = (BatchJobInfo) getValueAt(selectedRow, 2);
                fillDetailsFromCriteria(info.getReader());
                _detailPanel.setStatus("" + info.getStatus());
                _detailPanel.setRemarks(info.getErrorMessage());
                _detailPanel.setStatusTimestamp(info.getStatusTimestamp());
                _detailPanel.setLogFileName(NullUtils.getValue(info.getLogFileName(), ""));
                _detailPanel.setReportFileName(NullUtils.getValue(info.getReportFileName(), ""));
            }
        }
    }

    private void fillDetailsFromCriteria(final SearchCriteriaReader criteria) {
        if (criteria != null) {
            _detailPanel.setRequestorName(criteria.getRequester());

            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final Date requestDateTime = criteria.getRequestDateTime();
            if (requestDateTime != null) {
                _detailPanel.setRequestTimestamp(sdf.format(requestDateTime));
            }

            final String outputFolder = NullUtils.getValue(criteria.getResultLocation(), "");
            if (!outputFolder.isEmpty()) {
                final File outputFolderAsFile = new File(outputFolder);
                if (outputFolderAsFile.isAbsolute())
                    _detailPanel.setOutputFolderName(outputFolder);
                else {
                    final File resolvedOutputFolder = new File(SmartRankRestrictions.getOutputRootFolder(), outputFolder);
                    _detailPanel.setOutputFolderName(resolvedOutputFolder.getAbsolutePath());
                }
            }
        }
    }

    public void moveRow(final int from, final int to) {
        final BatchProcessingTableModel model = (BatchProcessingTableModel) getModel();
        model.moveRow(from, from, to);
        if (isRowSelected(from)) {
            getSelectionModel().setSelectionInterval(to, to);
        }
    }

    public void removeRow(final int row) {
        final BatchProcessingTableModel model = (BatchProcessingTableModel) getModel();
        model.removeRow(row);
    }
}
