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
package nl.minvenj.nfi.smartrank.io.databases.codis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import nl.minvenj.nfi.smartrank.io.databases.RecordValidator;

import nl.minvenj.nfi.smartrank.analysis.ExcludedProfile;
import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.CSVReader;
import nl.minvenj.nfi.smartrank.messages.data.EnabledLociMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

class CodisSampleIterator implements Iterator<Sample> {

    private final CSVReader _reader;
    private String[] _fields;
    private final String[] _headers;
    private final List<ExcludedProfile> _badRecords;
    Collection<String> _enabledLoci;
    int _curIndex = 0;

    public CodisSampleIterator(final CSVReader csvReader, final List<ExcludedProfile> badRecords) throws IOException {
        _reader = csvReader;
        // read headers
        _headers = _reader.readFields();
        for (int headerIdx = 0; headerIdx < _headers.length; headerIdx++) {
            _headers[headerIdx] = _headers[headerIdx].replaceFirst("_[1234]$", "").toUpperCase();
        }

        _badRecords = badRecords;
        _enabledLoci = MessageBus.getInstance().query(EnabledLociMessage.class);
    }

    @Override
    public boolean hasNext() {
        try {
            do {
                _fields = _reader.readFields();
                _curIndex++;
            } while (contains(_badRecords, _curIndex));
        }
        catch (final IOException ex) {
            throw new IllegalArgumentException("Error reading from " + _reader.getFileName(), ex);
        }
        return _fields != null;
    }

    private boolean contains(final List<ExcludedProfile> badRecords, final int recordNumber) {
        for (int idx = 0; idx < badRecords.size(); idx++) {
            if (badRecords.get(idx).getRecordNumber() == recordNumber)
                return true;
        }
        return false;
    }

    @Override
    public Sample next() {
        Sample sample = null;
        try {
            sample = new Sample(_fields[0].trim());

            int idx = 1;
            while (idx < _fields.length) {
                final String allele = _fields[idx].trim();
                if (!allele.equalsIgnoreCase("NULL")) {
                    final String locusName = _headers[idx];
                    Locus locus = sample.getLocus(locusName);
                    if (locus == null) {
                        locus = new Locus(locusName);
                        sample.addLocus(locus);
                    }
                    if (allele.matches(RecordValidator.VALID_ALLELE_REGEX)) {
                        locus.addAllele(new Allele(allele));
                    }
                }
                idx++;
            }

            // Perform a sanity check on the loci in the sample.
            final Collection<Locus> loci = new ArrayList<>(sample.getLoci()); // Note: Wrap in a new ArrayList to allow the remove to work
            for (final Locus locus : loci) {
                // Empty loci and loci with more than 2 alleles are removed
                if (locus.size() == 0 || locus.size() > 2) {
                    sample.removeLocus(locus);
                }
                // Loci with one allele are assumed to be homozygotic
                if (locus.size() == 1) {
                    locus.addAllele(locus.getAlleles().iterator().next());
                }
            }
        }
        catch (final Exception e) {
        }
        return sample;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported.");
    }
}
