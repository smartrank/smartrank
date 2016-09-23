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
package nl.minvenj.nfi.smartrank.io.databases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import nl.minvenj.nfi.smartrank.analysis.ExcludedProfile;
import nl.minvenj.nfi.smartrank.analysis.ExclusionReason;

/**
 * Validates records in the database.
 */
public class RecordValidator extends Thread {

    public static final String VALID_ALLELE_REGEX = "\\d{0,2}(\\.\\d)?";
    private final DatabaseValidationEventListener _listener;
    private final List<ExcludedProfile> _badRecords;
    private final BlockingQueue<RecordData> _queue;
    private boolean _done;
    private final String[] _locusNames;
    private final String[] _alleleOrdinals;
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
    public RecordValidator(final String[] headers, final BlockingQueue<RecordData> queue, final List<ExcludedProfile> badRecordList, final DatabaseValidationEventListener listener) {
        _listener = listener;
        _badRecords = badRecordList;
        _queue = queue;
        _locusNames = new String[headers.length];
        _alleleOrdinals = new String[headers.length];
        for (int idx = 0; idx < headers.length; idx++) {
            _locusNames[idx] = headers[idx].replaceFirst("_[1234]$", "").toUpperCase();
            _alleleOrdinals[idx] = headers[idx].replaceFirst(".*_", "");
        }
        _specimenCountPerNumberOfLoci = new ArrayList<>();
        _specimenCountPerLocus = new HashMap<>();
    }

    @Override
    public void run() {
        while (!(_done && _queue.isEmpty())) {
            RecordData recordData = null;
            try {
                recordData = _queue.poll(50, TimeUnit.MILLISECONDS);
            }
            catch (final InterruptedException ie) {

            }
            if (recordData != null) {
                String sampleName = null;
                String currentLocusName = "";
                final String[] fields = recordData.getFields();
                final int recordNumber = recordData.getRecordNumber();

                final ArrayList<String> loci = new ArrayList<>();

                try {
                    sampleName = fields[0].trim();

                    // Check if the number of fields in the current record matches that in the header
                    if (fields.length != _locusNames.length) {
                        _listener.onProblem(recordNumber, fields[0].trim(), "", String.format("Specimen excluded as it has %d fields, but %d were expected!", fields.length, _locusNames.length));
                        synchronized (_badRecords) {
                            _badRecords.add(new ExcludedProfile(fields[0].trim(), recordNumber, ExclusionReason.UNEXPECTED_NUMBER_OF_FIELDS));
                        }
                        continue;
                    }

                    int fieldIndex = 1;
                    int numberOfAllelesPerLocus = 0;
                    final ArrayList<String> alleleValues = new ArrayList<>();
                    while (fieldIndex < fields.length) {
                        if (((fieldIndex - 1) % 4) == 0) {
                            // Check if the fields are all valid alleles and that the record has no more than 2 alleles
                            if (numberOfAllelesPerLocus > 2) {
                                _listener.onProblem(recordNumber, sampleName, currentLocusName, "Locus excluded as it has " + numberOfAllelesPerLocus + " alleles: " + alleleValues);
                            }

                            if (numberOfAllelesPerLocus > 0) {
                                loci.add(currentLocusName);

                                Integer specimensHavingThisLocus = _specimenCountPerLocus.get(currentLocusName);
                                if (specimensHavingThisLocus == null) {
                                    specimensHavingThisLocus = 0;
                                }
                                _specimenCountPerLocus.put(currentLocusName, specimensHavingThisLocus + 1);
                            }

                            numberOfAllelesPerLocus = 0;
                            alleleValues.clear();
                        }

                        currentLocusName = _locusNames[fieldIndex];

                        // Check if the fields are all valid alleles and that the record has no more than 2 alleles
                        final String alleleOrdinal = _alleleOrdinals[fieldIndex];
                        if (!fields[fieldIndex].equalsIgnoreCase("NULL")) {
                            final String alleleValue = fields[fieldIndex].trim();
                            if (alleleValue.matches(VALID_ALLELE_REGEX)) {
                                alleleValues.add(alleleValue);
                                numberOfAllelesPerLocus++;
                            }
                            else {
                                _listener.onProblem(recordNumber, sampleName, currentLocusName, "Allele #" + alleleOrdinal + " ignored as it has an invalid format: '" + alleleValue + "'");
                            }
                        }

                        fieldIndex++;
                    }

                    // Check the last locus
                    if (numberOfAllelesPerLocus > 2) {
                        _listener.onProblem(recordNumber, sampleName, currentLocusName, "Locus excluded as it has " + numberOfAllelesPerLocus + " alleles: " + alleleValues);
                    }

                    if (numberOfAllelesPerLocus > 0) {
                        loci.add(currentLocusName);

                        Integer specimensHavingThisLocus = _specimenCountPerLocus.get(currentLocusName);
                        if (specimensHavingThisLocus == null) {
                            specimensHavingThisLocus = 0;
                        }
                        _specimenCountPerLocus.put(currentLocusName, specimensHavingThisLocus + 1);
                    }
                }
                catch (final Throwable e) {
                    _listener.onProblem(recordNumber, sampleName, currentLocusName, "Specimen excluded due to error: " + e.getClass().getName() + " - " + e.getLocalizedMessage());
                    synchronized (_badRecords) {
                        _badRecords.add(new ExcludedProfile(sampleName, recordNumber, ExclusionReason.OTHER));
                    }
                }
                while (_specimenCountPerNumberOfLoci.size() < (loci.size() + 1)) {
                    _specimenCountPerNumberOfLoci.add(0);
                }
                final Integer currentCount = _specimenCountPerNumberOfLoci.get(loci.size());
                _specimenCountPerNumberOfLoci.set(loci.size(), currentCount + 1);
            }
        }
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
