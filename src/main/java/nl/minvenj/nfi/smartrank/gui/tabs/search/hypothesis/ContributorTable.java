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

package nl.minvenj.nfi.smartrank.gui.tabs.search.hypothesis;

import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class ContributorTable extends ZebraTable {

    public ContributorTable() {
        setCellSelectionEnabled(false);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        if (!enabled && isEditing()) {
            editingStopped(null);
        }
        super.setEnabled(enabled);
    }

    @Override
    public boolean isCellEditable(final int row, final int column) {

        final int modelRow = convertRowIndexToModel(row);
        final int modelColumn = convertColumnIndexToModel(column);

        if ((modelColumn == 0 && modelRow == 0) || modelColumn == 1) {
            return false;
        }

        final AnalysisParameters parameters = MessageBus.getInstance().query(AnalysisParametersMessage.class);

        if (modelColumn == 2) {
            final Object isContributor = getValueAt(modelRow, convertColumnIndexToModel(0));
            if (isContributor instanceof Boolean) {
                return (Boolean) isContributor && !SmartRankRestrictions.isAutomaticParameterEstimationEnabled() && !parameters.isAutomaticParameterEstimationToBePerformed();
            }
            return false;
        }
        return super.isCellEditable(row, column);
    }

    @Override
    public void setName(final String name) {
        super.setName(name);
        getColumn("Dropout").setCellEditor(new DropoutProbabilityCellEditor());
        getColumn("Dropout").setCellRenderer(new DropoutProbabilityCellRenderer());
        getColumn("Contributor").setCellRenderer(new ContributorCellRenderer());
        setRowSelectionAllowed(false);
    }
}
