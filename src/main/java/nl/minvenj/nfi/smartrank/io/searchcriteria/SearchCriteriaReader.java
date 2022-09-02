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
package nl.minvenj.nfi.smartrank.io.searchcriteria;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.domain.Sample;

/**
 * To be implemented by classes that read search criteria from files.
 */
public interface SearchCriteriaReader {
    /**
     * Gets a {@link List} of {@link Sample}s that are to be loaded as crimescene samples.
     *
     * @return a {@link List} of {@link Sample}s. The list can be empty, but is never null.
     */
    List<Sample> getCrimesceneSamples();

    /**
     * Gets a {@link List} of {@link Sample}s that are to be loaded as profiles for known contributors.
     *
     * @return a {@link List} of {@link Sample}s. The list can be empty, but is never null.
     */
    List<Sample> getKnownProfiles();

    /**
     * Gets a collection of sample names and associated dropout values for the contributors under Hp.
     *
     * @return a {@link Map} containing the sample names as key and the associated dropout probability as value, encoded on a {@link Double}. The map can be empty, but is never null.
     */
    Map<String, Double> getHpContributors();

    /**
     * Gets the number of unknown contributors under Hp.
     *
     * @return an int containing the number of unknown contributors under Hp
     */
    int getHpUnknowns();

    /**
     * Gets the dropout probability for the unknown contributors under Hp.
     *
     * @return a {@link Double} containing the dropout probability of the unknown contributors under Hp
     */
    Double getHpUnknownDropout();

    /**
     * Gets a collection of sample names and associated dropout values for the contributors under Hd.
     *
     * @return a {@link Map} containing the sample names as key and the associated dropout probability as value, encoded on a {@link Double}. The map can be empty, but is never null.
     */
    Map<String, Double> getHdContributors();

    /**
     * Gets the number of unknown contributors under Hd.
     *
     * @return an int containing the number of unknown contributors under Hd
     */
    int getHdUnknowns();

    /**
     * Gets the dropout probability for the unknown contributors under Hd.
     *
     * @return a {@link Double} containing the dropout probability of the unknown contributors under Hd
     */
    Double getHdUnknownDropout();

    /**
     * Gets the location where the result files are to be written.
     *
     * @return the path where logfiles, reports and exported profiles are to be written, or null if this value was not present in the input file
     */
    String getResultLocation();

    /**
     * Gets the dropout of the Candidate under Hp.
     *
     * @return a {@link Double} containing the candidate's dropout value
     */
    Double getCandidateDropout();

    /**
     * Gets the value for Theta.
     *
     * @return a {@link Double} containing the value to use for the theta correction, or null if this value was not present in the input file
     */
    Double getTheta();

    /**
     * Gets the value to use for the dropin probability.
     *
     * @return a {@link Double} containing the value to use for the dropin probability, or null if this value was not present in the input file
     */
    Double getDropin();

    /**
     * Gets the value for the LR Threshold. LRs below this value will not be reported.
     *
     * @return an int containing the minimum LR to report.
     */
    Integer getLRThreshold();

    /**
     * Gets the value for the rare allele frequency.
     *
     * @return a double.
     */
    Double getRareAlleleFrequency();

    /**
     * Indicates whether parameter estimation is to be performed automatically.
     *
     * @return true if parameter estimation is to be performed automatically.
     */
    boolean isAutomaticParameterEstimationToBePerformed();

    /**
     * Gets the maximum number of results to be returned in the report and the GUI.
     *
     * @return the maximum number of results to be returned in the report, or -1 if this value is not present in the search criteria file.
     */
    int getMaximumNumberOfResults();

    /**
     * Gets the population statistics embedded in the import file.
     *
     * @return a {@link PopulationStatistics} object representing the statistics in the import file, or null if no statistics are present in the file.
     */
    PopulationStatistics getPopulationStatistics();

    /**
     * Gets the ID of the requester (if available in the input file)
     *
     * @return a String containing the id of the requester
     */
    String getRequester();

    /**
     * Gets the date and time of a search request (if available in the input file)
     *
     * @return a {@link Date} containing the date and time that the search criteria file was created, or null if this was not present in the file
     */
    Date getRequestDateTime();

    /**
     * Gets the properties defined in the search criteria data.
     *
     * @return A {@link Properties} object containing any defined additional properties. Never null.
     */
    Properties getProperties();
}
