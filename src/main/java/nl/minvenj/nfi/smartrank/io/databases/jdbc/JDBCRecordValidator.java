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
package nl.minvenj.nfi.smartrank.io.databases.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import nl.minvenj.nfi.smartrank.analysis.ExcludedProfile;
import nl.minvenj.nfi.smartrank.analysis.ExclusionReason;
import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseValidationEventListener;
import nl.minvenj.nfi.smartrank.messages.status.DetailStringMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

/**
 * Validates records in the database.
 */
public class JDBCRecordValidator extends Thread {

    public static final String VALID_ALLELE_REGEX = "\\d{0,2}(\\.\\d)?";
    private final DatabaseValidationEventListener _listener;
    private final List<ExcludedProfile> _badRecords;
    private final BlockingQueue<Sample> _queue;
    private boolean _done;
    private final ArrayList<Integer> _specimenCountPerNumberOfLoci;
    private final HashMap<String, Integer> _specimenCountPerLocus;

    /**
     * Constructor.
     *
     * @param headers an array of Strings containing the field headers from the database file
     * @param queue A queue holding the record definitions to be validated
     * @param badRecordList a list of record numbers for those records that are deemed invalid
     * @param listener a {@link DatabaseValidationEventListener} that will be called when a record fails validation
     */
    public JDBCRecordValidator(final BlockingQueue<Sample> queue, final List<ExcludedProfile> badRecordList, final DatabaseValidationEventListener listener) {
        _listener = listener;
        _badRecords = badRecordList;
        _queue = queue;
        _specimenCountPerNumberOfLoci = new ArrayList<>();
        _specimenCountPerLocus = new HashMap<>();
    }

    @Override
    public void run() {
        while (!(_done && _queue.isEmpty())) {
            Sample sample = null;
            try {
                sample = _queue.poll(50, TimeUnit.MILLISECONDS);
                if (sample != null) {
                    validateSample(sample);
                }
            }
            catch (final InterruptedException ie) {

            }
        }
    }

    private void validateSample(final Sample sample) {
        MessageBus.getInstance().send(this, new DetailStringMessage("Validating specimen " + sample.getName()));
        for (final Locus locus : sample.getLoci()) {
            try {
                if (locus.size() > 2) {
                    _listener.onProblem(sample.getName(), locus.getName(), "Locus excluded as it has " + locus.size() + " alleles: " + locus.getAlleles());
                }

                Integer specimensHavingThisLocus = _specimenCountPerLocus.get(locus.getName());
                if (specimensHavingThisLocus == null) {
                    specimensHavingThisLocus = 0;
                }
                _specimenCountPerLocus.put(locus.getName(), specimensHavingThisLocus + 1);

                // Check if the fields are all valid alleles and that the record has no more than 2 alleles
                for (final Allele allele : locus.getAlleles()) {
                    if (!allele.getAllele().matches(VALID_ALLELE_REGEX)) {
                        _listener.onProblem(sample.getName(), locus.getName(), "Allele '" + allele.getAllele() + "' ignored as it has an invalid format");
                    }
                }
            }
            catch (final Throwable e) {
                _listener.onProblem(sample.getName(), locus.getName(), "Specimen excluded due to error: " + e.getClass().getName() + " - " + e.getLocalizedMessage());
                synchronized (_badRecords) {
                    _badRecords.add(new ExcludedProfile(sample.getName(), ExclusionReason.OTHER));
                }
            }
        }

        while (_specimenCountPerNumberOfLoci.size() < (sample.size() + 1)) {
            _specimenCountPerNumberOfLoci.add(0);
        }

        final Integer currentCount = _specimenCountPerNumberOfLoci.get(sample.size());
        _specimenCountPerNumberOfLoci.set(sample.size(), currentCount + 1);
    }

    @Override
    public void interrupt() {
        _done = true;
        try {
            join();
        }
        catch (final InterruptedException e) {
        }
    }

    public List<Integer> getSpecimenCountPerNumberOfLoci() {
        return _specimenCountPerNumberOfLoci;
    }

    public Map<String, Integer> getSpecimenCountPerLocus() {
        return _specimenCountPerLocus;
    }
}
