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
package nl.minvenj.nfi.smartrank.domain;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nl.minvenj.nfi.smartrank.analysis.ExcludedProfile;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseReader;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseReaderFactory;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseValidationEventListener;

/**
 * Represents the DNA database.
 */
public class DNADatabase {

    private final DatabaseConfiguration _configuration;
    private DatabaseReader _reader;

    /**
     * Creates a file-based DNA database.
     *
     * @param dbFile the CSV file where the specimens of the DNA database reside
     */
    public DNADatabase(final File dbFile) {
        _configuration = new DatabaseConfiguration(dbFile);
    }

    /**
     * Creates a DBMS-based DNA database
     *
     * @param config a {@link DatabaseConfiguration} representing the link to the DMBS where the specimens of the database reside
     */
    public DNADatabase(final DatabaseConfiguration config) {
        _configuration = config;
    }

    /**
     * Validates the configuration and specimens within the DNA database.
     *
     * @param listener a {@link DatabaseValidationEventListener} that is notified of errors and progress
     * @throws IOException if the DNA database is not correctly configured
     * @throws InterruptedException if the validation process is interrupted
     */
    public void validate(final DatabaseValidationEventListener listener) throws IOException, InterruptedException {
        _reader = DatabaseReaderFactory.create(_configuration);
        _reader.validate(listener);
    }

    /**
     * Validates the configuration and specimens within the DNA database. This is usually done when changes to the database are detected after an initial validation.
     *
     * @param listener a {@link DatabaseValidationEventListener} that is notified of errors and progress
     * @throws IOException if the DNA database is not correctly configured
     * @throws InterruptedException if the validation process is interrupted
     */
    public void revalidate(final DatabaseValidationEventListener listener) throws IOException, InterruptedException {
        _reader.revalidate(listener);
    }

    /**
     * Gets a string describing the database connection.
     *
     * @return A string describing the database connection
     */
    public String getConnectString() {
        return _configuration.getConnectString();
    }

    /**
     * Get the configuration that was used to create this DNADatabase.
     *
     * @return a {@link DatabaseConfiguration} holding the settings for this database
     */
    public DatabaseConfiguration getConfiguration() {
        return _configuration;
    }

    /**
     * Gets the number of records in the database.
     *
     * @return an int containing the number of specimens in the database
     */
    public int getRecordCount() {
        return _reader.getRecordCount();
    }

    /**
     * Gets a breakdown of the database composition by number of loci in the profiles.
     *
     * @return a {@link List} of {@link Integer}s where value at index x holds the number of profiles having x loci.
     */
    public List<Integer> getSpecimenCountPerNumberOfLoci() {
        return _reader.getSpecimenCountPerNumberOfLoci();
    }

    /**
     * Gets a value that identifies the current contents of the database. This value is used to determine if a revalidation of the database is required before starting a search.
     * Depending on the underlying datastore this can be a SHA-1 hash, or some (derived) value from a database table.
     *
     * @return a String containing an identifying value for the current state of the database
     */
    public String getFileHash() {
        return _reader.getContentHash();
    }

    /**
     * Gets the name of the formatting of the database.
     *
     * @return a String identifying the format of the database
     */
    public String getFormatName() {
        return _reader.getFormatName();
    }

    /**
     * Gets an iterator over the specimens in the database.
     *
     * @return an {@link Iterator} over the {@link Sample}s in the database
     */
    public Iterator<Sample> iterator(final Properties properties) {
        return _reader.iterator(properties);
    }

    /**
     * Gets a list of records that are to be ignored due to formatting issues.
     *
     * @return a {@link List} of {@link ExcludedProfile} objects describing the specimen to ignore and the reason why it should be ignored
     */
    public List<ExcludedProfile> getBadRecordList() {
        return _reader.getBadRecordList();
    }

    /**
     * Gets a map containing the number of specimens containing a given locus.
     *
     * @return a {@link Map} with key {@link String} holding the names of all encountered loci and as value an {@link Integer} holding the number of specimens containing this locus
     */
    public Map<String, Integer> getSpecimenCountsPerLocus() {
        return _reader.getSpecimenCountsPerLocus();
    }

    public Map<String, Map<String, Integer>> getMetadataStatistics() {
        return _reader.getMetadataStatistics();
    }
}
