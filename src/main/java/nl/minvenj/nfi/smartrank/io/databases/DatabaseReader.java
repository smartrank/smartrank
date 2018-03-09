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
     * @return a String containing the name of the database format
     */
    public String getFormatName();

    /**
     * Gets a list of {@link ExcludedProfile}s describing the profiles excluded from the search.
     *
     * @return a {@link List} of {@link ExcludedProfile}s
     */
    public List<ExcludedProfile> getBadRecordList();

    /**
     * Gets a list of {@link Integer}s describing the number of specimens that were composed of the number of loci equal to the
     * current list index (e.g. the int at index 3 describes the number of specimens containing 3 loci).
     *
     * @return a {@link List} of {@link Integer}s
     */
    public List<Integer> getSpecimenCountPerNumberOfLoci();

    /**
     * Gets a map recording the number of specimens containing a given locus. The key of the map is a String
     * containing the locus name, the mapped value is an Integer holding the number of specimens containing that locus.
     *
     * @return a {@link Map} where the key is a {@link String} (locus name) and the value is an {@link Integer} (number of specimens with that locus)
     */
    public Map<String, Integer> getSpecimenCountsPerLocus();

    /**
     * Updates the metadata (e.g. number of specimen counts) for the database. It is at the discretion of the implementor whether to
     * make revalidation conditional to a change in the underlying datastore (file or database), but this is certainly recommended.
     *
     * @throws IOException if an error occurs accessing the underlying datastore
     */
    public void revalidate(DatabaseValidationEventListener listener) throws IOException, InterruptedException;

    /**
     * Gets a map of statistics values for profilemeta data. Can be empty if the selected reader does not support this feature.
     * @return A Map of Maps using Strings as key.
     */
    public Map<String, Map<String, Integer>> getMetadataStatistics();
}
