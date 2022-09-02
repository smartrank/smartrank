package nl.minvenj.nfi.smartrank.gui.tabs.database;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.general.DefaultPieDataset;

import nl.minvenj.nfi.smartrank.domain.DNADatabase;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable;

public class NumberOfLociPerSpecimenPane extends JSplitPane {

    private JFreeChart _chart;
    private ZebraTable _locusCountTable;

    public NumberOfLociPerSpecimenPane() {
        setContinuousLayout(true);
        setOrientation(JSplitPane.VERTICAL_SPLIT);
        setResizeWeight(0.5);

        _chart = ChartFactory.createPieChart3D("Database composition by number of loci", new DefaultPieDataset(), false, false, false);
        _chart.setAntiAlias(true);
        _chart.setTextAntiAlias(true);

        final PiePlot3D plot = (PiePlot3D) _chart.getPlot();
        plot.setIgnoreZeroValues(true);
        plot.setLabelBackgroundPaint(Color.decode("0XF0F0F0"));
        plot.setBaseSectionOutlinePaint(Color.BLACK);
        plot.setDarkerSides(true);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setNoDataMessage("No database loaded");

        setLeftComponent(new ChartPanel(_chart));

        _locusCountTable = new ZebraTable();
        _locusCountTable.setName("locusCountTable");
        _locusCountTable.setAutoCreateRowSorter(true);
        _locusCountTable.setDefaultRenderer(Double.class, new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                final JLabel label = new JLabel(String.format("%2.2f %%", value), SwingConstants.RIGHT);
                return label;
            }
        });
        _locusCountTable.setModel(new DefaultTableModel(new Object[]{"Number of Loci", "Number of Specimens", "Percentage"}, 0) {
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

        setRightComponent(new JScrollPane(_locusCountTable));
    }

    public void setDatabase(final DNADatabase db) {
        final DefaultPieDataset dataset = new DefaultPieDataset();

        _chart.addSubtitle(new TextTitle("Total number of records:" + db.getRecordCount()));

        final ArrayList<SpecimenCount> counts = new ArrayList<>();

        final DefaultTableModel model = (DefaultTableModel) _locusCountTable.getModel();
        _locusCountTable.setRowCount(0);

        final List<Integer> numberOfLoci = db.getSpecimenCountPerNumberOfLoci();
        for (int locusCount = 0; locusCount < numberOfLoci.size(); locusCount++) {
            final Integer specimenCount = numberOfLoci.get(locusCount);
            if (specimenCount > 0) {
                counts.add(new SpecimenCount(locusCount, specimenCount));
                final int percent = (int) (((long) specimenCount * 10000L) / db.getRecordCount());
                model.addRow(new Object[]{locusCount, specimenCount, percent / 100F});
            }
        }

        Collections.sort(counts);

        int underThreshold = 0;
        final ArrayList<SpecimenCount> toRemove = new ArrayList<SpecimenCount>();
        for (final SpecimenCount count : counts) {
            if (count._locusCount < SmartRankRestrictions.getMinimumNumberOfLoci()) {
                underThreshold += count._specimenCount;
                toRemove.add(count);
            }
        }
        counts.removeAll(toRemove);

        int otherCount = 0;
        while (counts.size() > 9) {
            final SpecimenCount count = counts.remove(0);
            otherCount += count._specimenCount;
        }

        for (final SpecimenCount count : counts) {
            final int percent = (int) ((count._specimenCount * 10000L) / db.getRecordCount());
            dataset.setValue(new LocusCount(count._locusCount, percent), count._specimenCount);
        }

        if (otherCount > 0) {
            final int percent = (int) ((otherCount * 10000L) / db.getRecordCount());
            dataset.setValue(new LocusCount(-1, percent), otherCount);
        }

        if (underThreshold > 0) {
            final int percent = (int) ((underThreshold * 10000L) / db.getRecordCount());
            dataset.setValue(new LocusCount(-2, percent), underThreshold);
        }

        ((PiePlot3D) _chart.getPlot()).setDataset(dataset);
    }

    private class SpecimenCount implements Comparable<SpecimenCount> {

        private final int _locusCount;
        private final int _specimenCount;

        public SpecimenCount(final int locusCount, final int specimenCount) {
            _locusCount = locusCount;
            _specimenCount = specimenCount;
        }

        @Override
        public int compareTo(final SpecimenCount other) {
            if (_specimenCount == other._specimenCount)
                return 0;
            if (_specimenCount > other._specimenCount)
                return 1;
            return -1;
        }

        @Override
        public String toString() {
            return _locusCount + " loci: " + _specimenCount + "specimens";
        }
    }

    private class LocusCount implements Comparable<LocusCount> {

        private final int _locusCount;
        private final int _fraction;

        public LocusCount(final int locusCount, final int fraction) {
            _locusCount = locusCount;
            _fraction = fraction;

        }

        @Override
        public int compareTo(final LocusCount other) {
            if (_locusCount == other._locusCount)
                return 0;
            if (_locusCount < other._locusCount)
                return 1;
            return -1;
        }

        @Override
        public String toString() {
            if (_locusCount == -1)
                return "Other\n" + _fraction / 100F + "%";
            if (_locusCount == -2)
                return "< " + SmartRankRestrictions.getMinimumNumberOfLoci() + "\n" + _fraction / 100F + "%";
            return _locusCount + " loci\n" + ((_fraction) / 100F) + "%";
        }
    }

    public void clear() {
        final DefaultPieDataset dataset = new DefaultPieDataset();
        for (final Object title : _chart.getSubtitles()) {
            if (title instanceof Title) {
                _chart.removeSubtitle((Title) title);
            }
        }
        ((PiePlot3D) _chart.getPlot()).setDataset(dataset);
        _locusCountTable.setRowCount(0);
    }

    public void save(final String outFileName, final FileOutputStream fos) throws IOException {
        fos.write("Number of loci per specimen\n".getBytes());
        fos.write("==============+============\n".getBytes());
        for (int row = 0; row < _locusCountTable.getModel().getRowCount(); row++) {
            fos.write(String.format("  %10d  |  %d\n", _locusCountTable.getValueAt(row, 0), _locusCountTable.getValueAt(row, 1)).getBytes());
        }
        fos.write("\n".getBytes());

        ChartUtilities.saveChartAsPNG(new File(outFileName.replaceAll(".txt", "-SampleSizeDistribution.png")), _chart, 800, 600);
    }

    @Override
    protected void printComponent(final Graphics g) {
        super.printComponent(g);
    }
}
