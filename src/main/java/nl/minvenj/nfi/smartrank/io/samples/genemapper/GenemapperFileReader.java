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
package nl.minvenj.nfi.smartrank.io.samples.genemapper;

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

class GenemapperFileReader implements SampleFileReader {

    private static final Logger LOG = LoggerFactory.getLogger(GenemapperFileReader.class);
    private static final String HEIGHT = "Height";
    private static final String OFF_LADDER_ALLELE = "OL";

    private final LinkedHashMap<String, Sample> _samples = new LinkedHashMap<>();
    private String _caseNumber;
    private boolean _initialized;
    private String _fileHash;
    private final File _file;

    public GenemapperFileReader(final File file) {
        _file = file;
    }

    @Override
    public String getFileHash() throws IOException {
        init();
        return _fileHash;
    }

    @Override
    public Collection<Sample> getSamples() throws IOException {
        init();
        return _samples.values();
    }

    @Override
    public String getCaseNumber() throws IOException {
        init();
        return _caseNumber;
    }

    @Override
    public File getFile() {
        return _file;
    }

    /**
     * Reads a Genemapper file
     *
     * @param fileName The name of the file. used as distinguishing feature
     * between samples from various files. Needs not be unique.
     * @param reader A CSVReader for getting the file contents
     */
    private void init() throws IOException {
        if (!_initialized) {
            _initialized = true;
            LOG.debug("Reading Genemapper file");

            _caseNumber = "";

            final CSVReader reader = new CSVReader(_file);

            // Read headers
            final String[] headers = reader.readFields();

            // Read lines and store loci and alleles in samples until no more line available
            String[] fields;
            int replicateId = 1;
            boolean hasReplicates = false;
            while ((fields = reader.readFields()) != null) {
                // Get sample name in order to extract the sample id and case number
                final String sampleName = fields[1];
                final String[] components = sampleName.split("_");
                String baseId = sampleName;
                if (components.length == 3) {
                    if (_caseNumber.isEmpty()) {
                        _caseNumber = components[1];
                    } else if (!_caseNumber.equalsIgnoreCase(components[1])) {
                        throw new IllegalArgumentException("Different case numbers present in this file: " + _caseNumber + " and " + components[1]);
                    }
                    baseId = components[2];
                }

                // We may have a file containing replicates but we may also have a file containing profiles
                // In the first case, we will see a number of samples with the same name. In this case we should append some replicate counter.
                // If we have seen this sample ID before...
                if (!hasReplicates && _samples.containsKey(baseId)) {
                    // Remove the sample from the list
                    final Sample remove = _samples.remove(baseId);
                    // Change the name of the sample by appending a replicate counter
                    remove.setName(baseId + "_Rep" + replicateId++);
                    // Put the sample back into the list under its new name
                    _samples.put(remove.getName(), remove);
                    // Signal that we have a file containing replicates, so we do not have to perform this check more than once
                    hasReplicates = true;
                }

                final Sample sample = new Sample(baseId + (hasReplicates ? "_Rep" + replicateId++ : ""), _file.getAbsolutePath());

                int fieldIndex = 2;
                while (fieldIndex < fields.length) {
                    final String locusName = headers[fieldIndex].toUpperCase();
                    final String[] alleles = reader.parse(fields[fieldIndex++]);
                    String[] heights = null;

                    if (fieldIndex < fields.length && HEIGHT.equalsIgnoreCase(headers[fieldIndex])) {
                        heights = reader.parse(fields[fieldIndex++]);
                    }

                    final Locus locus = new Locus(locusName);
                    for (int alleleIndex = 0; alleleIndex < alleles.length; alleleIndex++) {
                        final String allele = alleles[alleleIndex].trim();
                        if (allele.length() > 0 && !allele.toUpperCase().startsWith(OFF_LADDER_ALLELE)) {
                            locus.addAllele(new Allele(allele, heights == null ? 0.0F : Float.parseFloat(heights[alleleIndex])));
                        }
                    }

                    sample.addLocus(locus);
                }

                _fileHash = reader.getFileHash();
                _samples.put(sample.getName(), sample);
            }
            for (final Sample sample : _samples.values()) {
                sample.setSourceFileHash(_fileHash);
            }
        }
    }
}
