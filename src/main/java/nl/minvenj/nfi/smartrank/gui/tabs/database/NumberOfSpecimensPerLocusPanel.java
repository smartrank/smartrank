package nl.minvenj.nfi.smartrank.gui.tabs.database;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import nl.minvenj.nfi.smartrank.domain.DNADatabase;
import nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable;

public class NumberOfSpecimensPerLocusPanel extends JSplitPane {

    private final JFreeChart _chart;
    private ZebraTable _locusCountTable;

    public NumberOfSpecimensPerLocusPanel() {
        setContinuousLayout(true);
        setOrientation(JSplitPane.VERTICAL_SPLIT);
        setResizeWeight(0.5);

        _chart = ChartFactory.createBarChart3D("Specimens per locus", // chart title
                                               "Locus", // domain axis label
                                               "Number of specimens", // range axis label
                                               new DefaultCategoryDataset(), // data
                                               PlotOrientation.HORIZONTAL, // orientation
                                               false, // include legend
                                               false, // tooltips?
                                               false // URLs?
            );
        _chart.setAntiAlias(true);
        _chart.setTextAntiAlias(true);
        _chart.setBackgroundPaint(Color.WHITE);
        _chart.getPlot().setBackgroundPaint(Color.WHITE);
        _chart.getPlot().setNoDataMessage("No database loaded");

        final ChartPanel chartPanel = new ChartPanel(_chart);
        chartPanel.setMinimumDrawWidth(0);
        chartPanel.setMinimumDrawHeight(0);

        setLeftComponent(chartPanel);

        _locusCountTable = new ZebraTable();
        _locusCountTable.setName("locusCountTable");
        _locusCountTable.setAutoCreateRowSorter(true);
        _locusCountTable.setDefaultRenderer(Double.class, new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                return new JLabel(String.format("%2.2f %%", value), SwingConstants.RIGHT);
            }
        });
        _locusCountTable.setModel(new DefaultTableModel(new Object[]{"Locus", "Number of Specimens", "Percentage"}, 0) {
            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                if (columnIndex == 2)
                    return Double.class;
                return Integer.class;
            }

            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        });

        _locusCountTable.getRowSorter().addRowSorterListener(new RowSorterListener() {
            @Override
            public void sorterChanged(final RowSorterEvent e) {
                updateChart();
            }
        });

        setRightComponent(new JScrollPane(_locusCountTable));

    }

    public void setDatabase(final DNADatabase db) {
        final Map<String, Integer> specimenCountsPerLocus = db.getSpecimenCountsPerLocus();
        for (final String locusName : specimenCountsPerLocus.keySet()) {
            final int count = specimenCountsPerLocus.get(locusName);
            final int percent = (int) ((count * 10000L) / db.getRecordCount());
            _locusCountTable.addRow(new Object[]{locusName, count, percent / 100F});
        }
        updateChart();
    }

    private void updateChart() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int row = 0; row < _locusCountTable.getRowCount(); row++) {
            final String locus = (String) _locusCountTable.getValueAt(row, 0);
            final Integer count = (Integer) _locusCountTable.getValueAt(row, 1);
            dataset.addValue(count, "", locus);
        }
        _chart.getCategoryPlot().setDataset(dataset);
    }

    public void clear() {
        _chart.getCategoryPlot().setDataset(new DefaultCategoryDataset());
        _locusCountTable.setRowCount(0);
    }

    public void save(final String outFileName, final FileOutputStream fos) throws IOException {
        fos.write("Number of specimens per locus\n".getBytes());
        fos.write("==============+==============\n".getBytes());
        for (int row = 0; row < _locusCountTable.getModel().getRowCount(); row++) {
            fos.write(String.format("  %10.10s  |  %d\n", _locusCountTable.getValueAt(row, 0), _locusCountTable.getValueAt(row, 1)).getBytes());
        }
        fos.write("\n".getBytes());
        ChartUtilities.saveChartAsPNG(new File(outFileName.replaceAll(".txt", "-SamplesPerLocusDistribution.png")), _chart, 800, 600);
    }

}
