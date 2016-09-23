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
package nl.minvenj.nfi.smartrank.io.databases.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import nl.minvenj.nfi.smartrank.domain.Sample;

public class JDBCSampleIterator implements Iterator<Sample> {

    private ResultSet _resultSet;
    private final JDBCReader _reader;
    private Sample _nextSample;

    public JDBCSampleIterator(final JDBCReader reader) {
        _reader = reader;
    }

    @Override
    public boolean hasNext() {
        if (_nextSample == null) {
            try {
                _nextSample = _reader.readSample(getResultSet());
            }
            catch (final SQLException e) {
                return false;
            }
        }
        return (_nextSample != null);
    }

    private ResultSet getResultSet() throws SQLException {
        if (_resultSet == null) {
            _resultSet = _reader.getResultSet();
        }
        return _resultSet;
    }

    @Override
    public Sample next() {
        final Sample s = _nextSample;
        _nextSample = null;
        return s;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported.");
    }

}
