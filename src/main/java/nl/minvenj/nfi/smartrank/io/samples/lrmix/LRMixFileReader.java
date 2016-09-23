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
package nl.minvenj.nfi.smartrank.io.samples.lrmix;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.CSVReader;
import nl.minvenj.nfi.smartrank.io.samples.SampleFileReader;

/**
 * This class reads the contents of a Comma Separated File and attempts to
 * interpret this as a collection of DNA profiles.
 */
class LRMixFileReader implements SampleFileReader {

    private static final Logger LOG = LoggerFactory.getLogger(LRMixFileReader.class);
    private static final String OFF_LADDER_ALLELE = "OL";

    private final LinkedHashMap<String, Sample> _samples = new LinkedHashMap<>();
    private final File _file;

    private String _fileHash;
    private boolean _initialized;

    LRMixFileReader(final File file) {
        _file = file;
    }

    /**
     * @return A collection of {@link Sample} objects representing the samples
     * in the file
     */
    @Override
    public Collection<Sample> getSamples() throws IOException {
        init();
        return _samples.values();
    }

    /**
     * @return The case number for the samples stored in the file. Note that the
     * LRMix format does not store case number information. For this filetype,
     * this method returns an empty string.
     */
    @Override
    public String getCaseNumber() {
        return "";
    }

    @Override
    public File getFile() {
        return _file;
    }

    /**
     * @return The hash of the input data
     * @throws java.io.IOException if an error occurs accessong the bytes of the
     * indicated URI.
     */
    @Override
    public String getFileHash() throws IOException {
        init();
        return _fileHash;
    }

    /**
     * Reads a file in LRMix format
     *
     * @param fileName The getName of the file. used as distinguishing feature
     * between samples from various files. Needs not be unique.
     * @param reader A CSVReader for getting the file contents
     * @throws IOException
     */
    private void init() throws IOException {
        if (!_initialized) {
            LOG.debug("Reading LRMix file");

            final CSVReader reader = new CSVReader(_file);

            // Skip headers and determine separator
            final String[] headers = reader.readFields();

            // Read lines and store loci and alleles in samples until no more line available
            String[] fields;
            while ((fields = reader.readFields()) != null) {
                final String sampleName = fields[0];
                final String locusName = fields[1].toUpperCase();

                Sample sample = _samples.get(sampleName);
                if (sample == null) {
                    sample = new Sample(sampleName, _file.getAbsolutePath());
                    sample.setSourceFileHash(reader.getFileHash());
                    _samples.put(sampleName, sample);
                }

                final Locus locus = new Locus(locusName);

                // Create alleles and add them to the locus
                for (int idx = 2; idx < fields.length; idx++) {
                    final String alleleName = fields[idx].trim();
                    // Do not add Off Ladder alleles
                    if (!alleleName.toUpperCase().startsWith(OFF_LADDER_ALLELE) && !alleleName.isEmpty()) {
                        final Allele allele = new Allele(alleleName);
                        locus.addAllele(allele);
                    }
                }

                // Add locus to the sample
                sample.addLocus(locus);
            }
            _fileHash = reader.getFileHash();
            _initialized = true;
        }
    }
}
