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

import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;

import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRChartCustomizer;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

/**
 * Marks the LR Threshold in the results distribution graph in the report.
 */
public class LRThresholdMarker implements JRChartCustomizer {
    private static final Color COLOR = Color.BLUE;
    private static final BasicStroke STROKE = new BasicStroke(0.8F, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1, new float[]{4F, 4F}, 0F);

    @Override
    public void customize(final JFreeChart chart, final JRChart jasperChart) {
        final XYPlot plot = chart.getXYPlot();

        final int threshold = MessageBus.getInstance().query(AnalysisParametersMessage.class).getLrThreshold();
        plot.addRangeMarker(new ValueMarker(Math.log10(threshold), COLOR, STROKE));
        final LegendTitle legend = new LegendTitle(new LegendItemSource() {

            @Override
            public LegendItemCollection getLegendItems() {
                final LegendItemCollection c = new LegendItemCollection();
                c.add(new LegendItem("LR Threshold (" + threshold + ", log10=" + Math.log10(threshold) + ")", null, null, null, new Line2D.Float(0, 0, 10, 0), STROKE, COLOR));
                return c;
            }
        });
        final Font titleFont = chart.getTitle().getFont();
        final Font legendFont = new Font(titleFont.getName(), Font.PLAIN, titleFont.getSize() - 3);
        legend.setItemFont(legendFont);
        chart.addLegend(legend);
    }
}
