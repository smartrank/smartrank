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

package nl.minvenj.nfi.smartrank.io.samples.codis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.HashingReader;
import nl.minvenj.nfi.smartrank.io.codis.jaxb.AlleleType;
import nl.minvenj.nfi.smartrank.io.codis.jaxb.CODISImportFile;
import nl.minvenj.nfi.smartrank.io.codis.jaxb.SpecimenType;
import nl.minvenj.nfi.smartrank.io.codis.jaxb.SpecimenType.LOCUS;
import nl.minvenj.nfi.smartrank.io.samples.SampleFileReader;

class CodisFileReader implements SampleFileReader {

    private static final Logger LOG = LoggerFactory.getLogger(CodisFileReader.class);

    private final File _file;
    private String _fileHash;
    private String _caseNumber;
    private boolean _initialized;
    private final ArrayList<Sample> _samples = new ArrayList<>();

    public CodisFileReader(final File file) {
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
        return Collections.unmodifiableList(_samples);
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

    private void init() throws FileNotFoundException {
        if (!_initialized) {
            _initialized = true;
            final HashingReader reader = new HashingReader(new java.io.FileReader(_file));
            final CODISImportFile f = JAXB.unmarshal(reader, CODISImportFile.class);
            if (f.getHEADERVERSION() == null) {
                LOG.error("{} is not a Codis xml file because the HEADERVERSION field is missing", _file.getName());
                throw new IllegalArgumentException("This is not a Codis xml file!");
            }
            final List<SpecimenType> specimens = f.getSPECIMEN();
            if (specimens == null || specimens.isEmpty()) {
                throw new IllegalArgumentException("No specimens found in this file!");
            }
            for (final SpecimenType s : f.getSPECIMEN()) {
                _caseNumber = s.getCASEID();
                final Sample sample = new Sample(s.getSPECIMENID(), _file.getAbsolutePath());
                for (final LOCUS locus : s.getLOCUS()) {

                    final String locusName = locus.getLOCUSNAME();
                    if (locusName == null || locusName.isEmpty()) {
                        LOG.error("{} is corrupt: found a locus without the LOCUSNAME element", _file.getName());
                        throw new IllegalArgumentException("This file is corrupt!");
                    }
                    final Locus newLoc = new Locus(locusName);
                    for (final AlleleType allele : locus.getALLELE()) {

                        final String alleleValue = allele.getALLELEVALUE();
                        if (alleleValue == null || alleleValue.isEmpty()) {
                            LOG.error("{} is corrupt: found an allele without the ALLELEVALUE element", _file.getName());
                            throw new IllegalArgumentException("This file is corrupt!");
                        }
                        newLoc.addAllele(new Allele(alleleValue));
                    }
                    sample.addLocus(newLoc);
                    sample.setSourceFileHash(reader.getHash());
                }
                _samples.add(sample);
            }
            _fileHash = reader.getHash();
        }
    }
}
