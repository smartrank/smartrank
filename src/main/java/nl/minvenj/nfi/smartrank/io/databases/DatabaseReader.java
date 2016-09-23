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

package nl.minvenj.nfi.smartrank.io.databases;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import nl.minvenj.nfi.smartrank.analysis.ExcludedProfile;
import nl.minvenj.nfi.smartrank.domain.Sample;

/**
 * To be implemented by classes that read specimens from a DNA database.
 */
public interface DatabaseReader extends Iterable<Sample> {

    /**
     * Validates the content of the database and reports problems and errors.
     *
     * @param listener a {@link DatabaseValidationEventListener} that is called to report progress and problems
     * @throws IOException if an error occurs reading from the underlying datasource
     * @throws InterruptedException if the validation process is interrupted
     */
    public void validate(DatabaseValidationEventListener listener) throws IOException, InterruptedException;

    /**
     * Counts the number of specimens in the database.
     *
     * @return an integer containing the number of specimens in the database
     */
    public int getRecordCount();

    /**
     * Reports a hash value calculated from the database contents
     * @return a String containing the hash value
     */
    public String getContentHash();

    /**
     * Reports the type of the database.
     *
     * @return a String containing the name of the
     */
    public String getFormatName();

    public List<ExcludedProfile> getBadRecordList();

    public List<Integer> getSpecimenCountPerNumberOfLoci();

    public Map<String, Integer> getSpecimenCountsPerLocus();
}
