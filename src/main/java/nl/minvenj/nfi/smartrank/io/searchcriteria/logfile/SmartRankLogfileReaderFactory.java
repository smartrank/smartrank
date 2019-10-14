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
package nl.minvenj.nfi.smartrank.io.searchcriteria.logfile;

import java.io.IOException;

import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReaderFactory;

/**
 * A factory for the class that reads search criteria from SmartRanklogfiles.
 */
public class SmartRankLogfileReaderFactory extends SearchCriteriaReaderFactory {

    @Override
    public boolean accepts(final String criteria) {
        return criteria.length() > 19 && criteria.substring(0, 19).matches("\\d\\d\\d\\d/\\d\\d/\\d\\d \\d\\d\\:\\d\\d\\:\\d\\d") && criteria.contains("SmartRank version") && criteria.contains("=================");
    }

    @Override
    protected SearchCriteriaReader newInstance(final String context, final String criteria) throws IOException {
        return new SmartRankLogfileReader(context, criteria);
    }
}
