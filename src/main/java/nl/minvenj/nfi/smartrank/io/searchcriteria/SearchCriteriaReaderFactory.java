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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import nl.minvenj.nfi.smartrank.io.searchcriteria.json.JsonReaderFactory;
import nl.minvenj.nfi.smartrank.io.searchcriteria.locim.SmartRankImportFileReaderFactory;
import nl.minvenj.nfi.smartrank.io.searchcriteria.logfile.SmartRankLogfileReaderFactory;
import nl.minvenj.nfi.smartrank.raven.NullUtils;

/**
 * Serves as a base class factory-classes that generate readers for files containing search criteria.
 */
public abstract class SearchCriteriaReaderFactory {

    // The list of registered reader factories
    private static final List<SearchCriteriaReaderFactory> REGISTERED_READERS = Arrays.asList(new SmartRankLogfileReaderFactory(), new SmartRankImportFileReaderFactory(), new JsonReaderFactory());

    // Private constructor so no-one can create this class except the class itself
    protected SearchCriteriaReaderFactory() {
    }

    /**
     * Tries to find a registered reader to read the supplied data.
     *
     * @param context some context information to record with the reader (e.g. a filename)
     * @param criteria The file containing search criteria
     * @return a {@link SearchCriteriaReader} to read the contents of the file
     * @throws IOException If no reader could be found that supports this file format
     */
    public static SearchCriteriaReader getReader(final String context, final String criteria) throws IOException {
        NullUtils.argNotNull(criteria, "criteria");
        for (final SearchCriteriaReaderFactory factory : REGISTERED_READERS) {
            if (factory.accepts(criteria)) {
                return factory.newInstance(context, criteria);
            }
        }
        throw new IllegalArgumentException("Data is not a supported format for reading search criteria: " + getSafeValue(criteria));
    }

    private static String getSafeValue(final String criteria) {
        String shortVersion = "'" + criteria.substring(0, Math.min(100, criteria.length()));
        if (shortVersion.length() != criteria.length()) {
            shortVersion += "...' (" + criteria.length() + " characters in total)";
        }
        else {
            shortVersion += "'";
        }
        return shortVersion;
    }

    /**
     * Tries to find a registered reader to read the supplied file.
     *
     * @param context some context information to record with the reader (e.g. a filename)
     * @param criteria The file containing search criteria
     * @return a {@link SearchCriteriaReader} to read the contents of the file
     * @throws IOException If no reader could be found that supports this file format
     */
    public static SearchCriteriaReader getReader(final File file) throws IOException {
        return getReader(file.getAbsolutePath(), FileUtils.readFileToString(file));
    }

    /**
     * Determines whether the factory can create a reader for the supplied criteria data
     *
     * @param criteria the criteria data to test
     * @return <code>true</code> if the factory can generate a reader for the supplied file
     */
    protected abstract boolean accepts(String criteria);

    /**
     * Creates a {@link SearchCriteriaReader} instance for the supplied file.
     *
     * @param context some context information to record with the reader (e.g. a filename)
     * @param criteria the data to generate a reader for
     * @return a {@link SearchCriteriaReader} containing the search criteria in the file
     * @throws IOException if an error occurs reading from the file
     */
    protected abstract SearchCriteriaReader newInstance(String context, String criteria) throws IOException;

}
