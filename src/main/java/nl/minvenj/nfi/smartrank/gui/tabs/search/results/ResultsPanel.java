/*
 * Copyright (C) 2015 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.gui.tabs.search.results;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.analysis.SearchResultCache;
import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.LikelihoodRatio;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.messages.data.CrimeSceneProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseMessage;
import nl.minvenj.nfi.smartrank.messages.data.DefenseHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.KnownProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.LRThresholdMessage;
import nl.minvenj.nfi.smartrank.messages.data.ProsecutionHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.ReportTopMessage;
import nl.minvenj.nfi.smartrank.messages.data.SearchResultsMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchCompletedMessage;
import nl.minvenj.nfi.smartrank.raven.annotations.ExecuteOnSwingEventThread;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

/**
 * A panel that shows the matching profiles of a search.
 */
public class ResultsPanel extends SmartRankPanel {

    private final ZebraTable _resultsTable;
    private final SearchResultCache _resultCache;
    private LikelihoodRatio[] _lrs;
    private JLabel _warningLabel;

    private int _lrThreshold;

    /**
     * Creates new form ResultsPanel
     */
    public ResultsPanel() {
        _resultCache = SearchResultCache.getInstance();

        setLayout(new MigLayout("", "[430px,grow,fill]", "[278px,grow,fill][]"));

        final JScrollPane resultsTableScrollPane = new JScrollPane();
        _resultsTable = new ZebraTable();
        _resultsTable.setModel(new DefaultTableModel(new String[]{"Rank", "Specimen ID", "LR"}, 0) {
            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                return columnIndex == 0 ? Integer.class : String.class;
            }

            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        });
        _resultsTable.setName("resultsTable"); // NOI18N

        resultsTableScrollPane.setViewportView(_resultsTable);
        add(resultsTableScrollPane, "cell 0 0,grow");

        _warningLabel = new JLabel("<html>Details for the specimens marked in&nbsp;<font bgColor='red' color='white'><b>&nbsp;red&nbsp;</b></font>&nbsp;will not be included in the report.");
        _warningLabel.setVisible(false);
        add(_warningLabel, "cell 0 1");

        _resultsTable.getColumn("Rank").setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                final JLabel label = new JLabel("" + value);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                final AnalysisParameters parameters = MessageBus.getInstance().query(AnalysisParametersMessage.class);
                if (parameters != null && row >= parameters.getMaximumNumberOfResults()) {
                    label.setText("<html><b><font color='white' bgColor='red'>" + label.getText());
                    _warningLabel.setVisible(true);
                }
                return label;
            }
        });

        _resultsTable.getColumn("Specimen ID").setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                final JLabel label = new JLabel("" + value);
                label.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 0), label.getBorder()));
                final AnalysisParameters parameters = MessageBus.getInstance().query(AnalysisParametersMessage.class);
                if (parameters != null && row >= parameters.getMaximumNumberOfResults()) {
                    label.setText("<html><b><font color='white' bgColor='red'>" + label.getText());
                    _warningLabel.setVisible(true);
                }
                return label;
            }
        });

        _resultsTable.getColumn("LR").setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                final JLabel label = new JLabel("" + value);
                label.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 0), label.getBorder()));
                final AnalysisParameters parameters = MessageBus.getInstance().query(AnalysisParametersMessage.class);
                if (parameters != null && row >= parameters.getMaximumNumberOfResults()) {
                    label.setText("<html><b><font color='white' bgColor='red'>" + label.getText());
                    _warningLabel.setVisible(true);
                }
                return label;
            }
        });

        _resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final LikelihoodRatio ratio = (LikelihoodRatio) _resultsTable.getValueAt(_resultsTable.getSelectedRow(), 2);
                            final Window topWindow = SwingUtilities.windowForComponent(ResultsPanel.this);
                            final ProfileDetailDialog dlg = new ProfileDetailDialog((Frame) topWindow, false, ratio);
                            dlg.setLocationRelativeTo(topWindow);
                            dlg.setVisible(true);
                        }
                    });
                }
            }
        });

        registerAsListener();
    }

    @RavenMessageHandler(SearchResultsMessage.class)
    @ExecuteOnSwingEventThread
    public void onResultsChanged(final SearchResults results) {
        if (results == null) {
            _lrs = new LikelihoodRatio[0];
        }
        else {
            _lrs = results.getPositiveLRs().toArray(new LikelihoodRatio[0]);
        }
        updateTable();
    }

    @RavenMessageHandler(SearchCompletedMessage.class)
    @ExecuteOnSwingEventThread
    public void onFinalResults(final SearchResults results) {
        _resultCache.put(results);
        updateTable();
    }

    @RavenMessageHandler(ReportTopMessage.class)
    @ExecuteOnSwingEventThread
    public void onChangeReportTop(final int top) {
        updateTable();
    }

    @RavenMessageHandler(LRThresholdMessage.class)
    @ExecuteOnSwingEventThread
    public void onChangeLRThreshold(final int threshold) {
        _lrThreshold = threshold;
        updateTable();
    }

    @RavenMessageHandler({DatabaseMessage.class, CrimeSceneProfilesMessage.class, KnownProfilesMessage.class})
    @ExecuteOnSwingEventThread
    private void clearResultsCache() {
        _resultCache.clear();
        clearResults();
    }

    @RavenMessageHandler({ProsecutionHypothesisMessage.class, DefenseHypothesisMessage.class})
    @ExecuteOnSwingEventThread
    private void clearResults() {
        final DefenseHypothesis hd = MessageBus.getInstance().query(DefenseHypothesisMessage.class);
        final ProsecutionHypothesis hp = MessageBus.getInstance().query(ProsecutionHypothesisMessage.class);
        final SearchResults searchResults = _resultCache.get(hp, hd);
        if (searchResults != null) {
            _lrs = searchResults.getPositiveLRs().toArray(new LikelihoodRatio[0]);
            updateTable();
        }
        else {
            _lrs = new LikelihoodRatio[0];
            _resultsTable.setRowCount(0);
        }
    }

    private void updateTable() {
        _warningLabel.setVisible(false);
        if (_lrs != null) {
            Arrays.sort(_lrs, new Comparator<LikelihoodRatio>() {
                @Override
                public int compare(final LikelihoodRatio o1, final LikelihoodRatio o2) {
                    return -o1.compareTo(o2);
                }
            });

            final int selectedRow = _resultsTable.getSelectedRow();
            Sample selectedSample = null;
            if (selectedRow >= 0) {
                selectedSample = (Sample) _resultsTable.getValueAt(selectedRow, 1);
            }

            _resultsTable.setRowCount(0);
            int idx = 1;
            for (final LikelihoodRatio ratio : _lrs) {
                if ((ratio.getOverallRatio().getRatio() > _lrThreshold)) {
                    _resultsTable.addRow(new Object[]{idx, ratio.getProfile(), ratio});
                    if (ratio.getProfile() == selectedSample) {
                        _resultsTable.setRowSelectionInterval(_resultsTable.getRowCount() - 1, _resultsTable.getRowCount() - 1);
                    }
                    idx++;
                }
            }
        }
    }
}
