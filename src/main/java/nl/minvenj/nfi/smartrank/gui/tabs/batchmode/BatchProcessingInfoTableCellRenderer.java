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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;
import nl.minvenj.nfi.smartrank.raven.NullUtils;

public class BatchProcessingInfoTableCellRenderer implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

        final BatchJobInfo info = (BatchJobInfo) value;
        if (!NullUtils.getValue(info.getErrorMessage(), "").isEmpty())
            return new JLabel(info.getErrorMessage());

        final SearchCriteriaReader reader = info.getReader();
        if (reader != null) {
            String requestedBy = reader.getRequester();
            if (requestedBy == null)
                requestedBy = "Unknown";

            final Date requestDate = reader.getRequestDateTime();
            String requestTimestamp = "Unknown Time";
            if (requestDate != null) {
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                requestTimestamp = sdf.format(requestDate);
            }
            return new JLabel("Requested by " + requestedBy + " at " + requestTimestamp);
        }
        else {
            return new JLabel("" + value);
        }
    }
}
