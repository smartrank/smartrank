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
package nl.minvenj.nfi.smartrank.gui.tabs.search.hypothesis;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class DropoutProbabilityCellRenderer extends JSpinner implements TableCellRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(DropoutProbabilityCellRenderer.class);

    public DropoutProbabilityCellRenderer() {
        setBorder(null);
        setOpaque(false);
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        final AnalysisParameters parameters = MessageBus.getInstance().query(AnalysisParametersMessage.class);
        if (!(table.getValueAt(row, 0) instanceof Boolean) || !(Boolean) table.getValueAt(row, 0)) {
            return new JLabel();
        }
        if (SmartRankRestrictions.isAutomaticParameterEstimationEnabled() || parameters.isAutomaticParameterEstimationToBePerformed()) {
            final JLabel label = new JLabel("automatic");
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            return label;
        }

        try {
            setModel(new SpinnerNumberModel((Double) value, new Double(SmartRankRestrictions.getDropoutMinimum()), new Double(SmartRankRestrictions.getDropoutMaximum()), new Double(0.01)));
        }
        catch (final Throwable t) {
            LOG.error("Error setting dropout value={}, min={}, max={}", value, SmartRankRestrictions.getDropoutMinimum(), SmartRankRestrictions.getDropoutMaximum());
        }
        return this;
    }
}
