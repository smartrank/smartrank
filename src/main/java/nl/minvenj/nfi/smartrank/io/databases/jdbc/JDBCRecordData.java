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
package nl.minvenj.nfi.smartrank.io.databases.jdbc;

import nl.minvenj.nfi.smartrank.domain.Sample;

public class JDBCRecordData {

    private final int _recordNumber;
    private final Sample _sample;

    public JDBCRecordData(final int recordNumber, final Sample sample) {
        _recordNumber = recordNumber;
        _sample = sample;
    }

    public Sample getSample() {
        return _sample;
    }

    public int getRecordNumber() {
        return _recordNumber;
    }
}
