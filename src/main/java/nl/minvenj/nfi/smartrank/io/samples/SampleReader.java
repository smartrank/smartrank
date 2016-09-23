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
package nl.minvenj.nfi.smartrank.io.samples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.samples.codis.CodisImportFileReaderFactory;
import nl.minvenj.nfi.smartrank.io.samples.genemapper.GenemapperReaderFactory;
import nl.minvenj.nfi.smartrank.io.samples.lrmix.LRMixReaderFactory;

/**
 * This class reads the contents of a Comma Separated File and attempts to
 * interpret this as a collection of DNA profiles.
 */
public class SampleReader {

    private static final Logger LOG = LoggerFactory.getLogger(SampleReader.class);
    private static final ArrayList<SampleFileReaderFactory> SAMPLE_FILE_READER_FACTORIES = new ArrayList<>();

    static {
        SAMPLE_FILE_READER_FACTORIES.add(new LRMixReaderFactory());
        SAMPLE_FILE_READER_FACTORIES.add(new GenemapperReaderFactory());
        SAMPLE_FILE_READER_FACTORIES.add(new CodisImportFileReaderFactory());
    }
    private SampleFileReader _reader;

    /**
     * Attempts to open and read the supplied file and parse the contents as a
     * sequence of DNA samples
     *
     * @param file The input file
     *
     * @throws FileNotFoundException If the file does not exist
     * @throws IOException           If there was an error reading from the file
     * @throws NullPointerException  If the file parameter was null
     */
    public SampleReader(File file) throws FileNotFoundException, IOException {
        if (file == null) {
            throw new NullPointerException("Input File was null");
        }

        if (!file.exists()) {
            throw new FileNotFoundException(file.toString());
        }
        for (SampleFileReaderFactory readerFactory : SAMPLE_FILE_READER_FACTORIES) {
            if (readerFactory.accepts(file)) {
                _reader = readerFactory.create(file);
                return;
            }
        }
        throw new IllegalArgumentException("Unknown file format for '" + file + "'");
    }

    /**
     * @return A collection of {@link Sample} objects representing the samples
     *         in the file
     */
    public Collection<Sample> getSamples() throws IOException {
        return _reader.getSamples();
    }

    /**
     * @return The case number for the samples stored in the file. Note that the
     *         LRMix format does not store case number information. For this
     *         filetype, this method returns an empty string.
     *
     * @throws java.io.IOException if an error occurs accessing the file
     */
    public String getCaseNumber() throws IOException {
        return _reader.getCaseNumber();
    }

    /**
     * @return The name of the file from which the samples were read
     */
    public File getFile() {
        return _reader.getFile();
    }

    /**
     * @return The hash of the input data
     */
    public String getFileHash() throws IOException {
        return _reader.getFileHash();
    }
}
