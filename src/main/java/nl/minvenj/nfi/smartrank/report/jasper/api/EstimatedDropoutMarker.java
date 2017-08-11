/**
 * Copyright (C) 2016 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.report.jasper.api;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Line2D;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;

import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRChartCustomizer;
import nl.minvenj.nfi.smartrank.messages.data.SearchResultsMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

/**
 * Marks the estimated dropout value, 5% 50% and 95% percentiles of the dropout distribution graph in the report.
 */
public class EstimatedDropoutMarker implements JRChartCustomizer {
    private static final BasicStroke ESTIMATED_DROPOUT_STROKE = new BasicStroke(1.1F);
    private static final BasicStroke CHARACTERISTICS_STROKE = new BasicStroke(0.6F, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1, new float[]{5, 2}, 0);

    @Override
    public void customize(final JFreeChart chart, final JRChart jasperChart) {
        final XYPlot plot = chart.getXYPlot();

        final int estimationPercentile = getEstimationPercentile();

        if (estimationPercentile != 5)
            plot.addDomainMarker(new ValueMarker(getPercentile(5).doubleValue(), Color.CYAN, CHARACTERISTICS_STROKE));
        if (estimationPercentile != 50)
            plot.addDomainMarker(new ValueMarker(getPercentile(50).doubleValue(), Color.GREEN, CHARACTERISTICS_STROKE));
        plot.addDomainMarker(new ValueMarker(getEstimatedDropoutValue().doubleValue(), Color.BLUE, ESTIMATED_DROPOUT_STROKE));
        if (estimationPercentile != 95)
            plot.addDomainMarker(new ValueMarker(getPercentile(95).doubleValue(), Color.MAGENTA, CHARACTERISTICS_STROKE));
        final LegendTitle legend = new LegendTitle(new LegendItemSource() {

            @Override
            public LegendItemCollection getLegendItems() {
                final LegendItemCollection c = new LegendItemCollection();
                final DecimalFormat decimalFormat = new DecimalFormat("#.##");
                if (estimationPercentile != 5) {
                    c.add(new LegendItem("5% (" + decimalFormat.format(getPercentile(5)) + ")", null, null, null, new Line2D.Float(0, 0, 10, 10), CHARACTERISTICS_STROKE, Color.CYAN));
                }
                if (estimationPercentile != 50) {
                    c.add(new LegendItem("50% (" + decimalFormat.format(getPercentile(50)) + ")", null, null, null, new Line2D.Float(0, 0, 10, 10), CHARACTERISTICS_STROKE, Color.GREEN));
                }
                c.add(new LegendItem(getDropoutEstimationPercentile() + "% (" + decimalFormat.format(getEstimatedDropoutValue()) + ")", null, null, null, new Line2D.Float(0, 0, 10, 10), ESTIMATED_DROPOUT_STROKE, Color.BLUE));
                if (estimationPercentile != 95) {
                    c.add(new LegendItem("95% (" + decimalFormat.format(getPercentile(95)) + ")", null, null, null, new Line2D.Float(0, 0, 10, 10), CHARACTERISTICS_STROKE, Color.MAGENTA));
                }
                return c;
            }
        });
        final Font titleFont = chart.getTitle().getFont();
        final Font legendFont = new Font(titleFont.getName(), Font.PLAIN, titleFont.getSize() - 3);
        legend.setItemFont(legendFont);
        chart.addLegend(legend);
    }

    private int getEstimationPercentile() {
        return MessageBus.getInstance().query(SearchResultsMessage.class).getParameters().getDropoutEstimation().getDropoutEstimationPercentile();
    }

    private int getDropoutEstimationPercentile() {
        return MessageBus.getInstance().query(SearchResultsMessage.class).getParameters().getDropoutEstimation().getDropoutEstimationPercentile();
    }

    private BigDecimal getEstimatedDropoutValue() {
        return MessageBus.getInstance().query(SearchResultsMessage.class).getParameters().getDropoutEstimation().getEstimatedDropout();
    }

    private BigDecimal getPercentile(final int percentile) {
        return MessageBus.getInstance().query(SearchResultsMessage.class).getParameters().getDropoutEstimation().getPercentile(percentile);
    }
}
