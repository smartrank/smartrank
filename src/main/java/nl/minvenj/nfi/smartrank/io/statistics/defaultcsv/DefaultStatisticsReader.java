/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.smartrank.io.statistics.defaultcsv;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.io.CSVReader;
import nl.minvenj.nfi.smartrank.io.statistics.StatisticsFileReader;

/**
 * This class reads population statistics information from a file
 */
public class DefaultStatisticsReader implements StatisticsFileReader {

    private final PopulationStatistics _stats;

    /**
     * Constructor
     *
     * @param fileName The name of the file containing the population statistics
     * @throws FileNotFoundException If the specified file is not found
     * @throws MalformedURLException If the filename is not properly formatted
     * @throws IOException If there was an error reading from the file
     */
    public DefaultStatisticsReader(final String fileName) throws FileNotFoundException, MalformedURLException, IOException {
        this(fileName, new CSVReader(fileName, true));
    }

    public DefaultStatisticsReader(final String fileName, final InputStream resource) throws IOException {
        this(fileName, new CSVReader(resource, true));
    }

    protected DefaultStatisticsReader(final String fileName, final CSVReader reader) throws IOException {
        _stats = new PopulationStatistics(fileName);
        final String[] headers = reader.readFields();
        String[] data = reader.readFields();

        while (data != null) {
            for (int locusIdx = 1; locusIdx < data.length; locusIdx++) {
                if (!data[locusIdx].isEmpty()) {
                    _stats.addStatistic(headers[locusIdx].toUpperCase(), data[0], new BigDecimal(data[locusIdx].replaceAll("\"", "").trim()));
                }
            }
            data = reader.readFields();
        }
        _stats.setFileHash(reader.getFileHash());
    }

    /**
     * @return A PopulationStatistics object
     */
    @Override
    public PopulationStatistics getStatistics() {
        return _stats;
    }
}