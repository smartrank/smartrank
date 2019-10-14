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
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.io.CSVReader;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseStatistics;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseValidationEventListener;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

/**
 * Validates records in the database.
 */
public class CodisRecordValidator implements Callable<DatabaseStatistics> {

    private static final Logger LOG = LoggerFactory.getLogger(CodisRecordValidator.class);

    public static final String VALID_ALLELE_REGEX = "\\d{0,2}(\\.\\d)?";
    private final DatabaseValidationEventListener _listener;
    private final String[] _locusNames;
    private final String[] _alleleOrdinals;
    private final CSVReader _reader;

    public CodisRecordValidator(final String[] headers, final CSVReader validationReader, final DatabaseValidationEventListener listener) {
        _reader = validationReader;
        _listener = listener;
        _locusNames = new String[headers.length];
        _alleleOrdinals = new String[headers.length];
        for (int idx = 0; idx < headers.length; idx++) {
            _locusNames[idx] = headers[idx].replaceFirst("_\\d+$", "").toUpperCase();
            _alleleOrdinals[idx] = headers[idx].replaceFirst(".*_", "");
        }
    }

    @Override
    public DatabaseStatistics call() {
        final DatabaseStatistics stat = new DatabaseStatistics();
        String[] fields;
        try {
            fields = _reader.readFields();
            while ((fields) != null) {
                String sampleName = null;
                String currentLocusName = "";

                final ArrayList<String> loci = new ArrayList<>();

                try {
                    sampleName = fields[0].trim();

                    // Check if the number of fields in the current record matches that in the header
                    if (fields.length != _locusNames.length) {
                        _listener.onProblem(fields[0].trim(), "", String.format("Specimen excluded as it has %d fields, but %d were expected!", fields.length, _locusNames.length));
                    }
                    else {
                        stat.increaseRecordCount();
                        int fieldIndex = 1;
                        int numberOfAllelesPerLocus = 0;
                        final ArrayList<String> alleleValues = new ArrayList<>();
                        while (fieldIndex < fields.length) {
                            // If we have a complete locus, perform some sanity checks.
                            // Note: the last locus will be checked after the 'while' block
                            if (!currentLocusName.isEmpty() && !currentLocusName.equalsIgnoreCase(_locusNames[fieldIndex])) {
                                checkNumberOfAlleles(sampleName, currentLocusName, numberOfAllelesPerLocus, alleleValues);
                                updateLocusStats(stat, currentLocusName, loci, numberOfAllelesPerLocus);
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
                                    _listener.onProblem(sampleName, currentLocusName, "Allele #" + alleleOrdinal + " ignored as it has an invalid format: '" + alleleValue + "'");
                                }
                            }

                            fieldIndex++;
                        }

                        checkNumberOfAlleles(sampleName, currentLocusName, numberOfAllelesPerLocus, alleleValues);
                        updateLocusStats(stat, currentLocusName, loci, numberOfAllelesPerLocus);
                        while (stat.getSpecimenCountPerNumberOfLoci().size() < (loci.size() + 1)) {
                            stat.getSpecimenCountPerNumberOfLoci().add(0);
                        }
                        final Integer currentCount = stat.getSpecimenCountPerNumberOfLoci().get(loci.size());
                        stat.getSpecimenCountPerNumberOfLoci().set(loci.size(), currentCount + 1);
                    }
                }
                catch (final Throwable e) {
                    _listener.onProblem(sampleName, currentLocusName, "Specimen excluded due to error: " + e.getClass().getName() + " - " + e.getLocalizedMessage());
                }
                fields = _reader.readFields();
            }
        }
        catch (final IOException e) {
            LOG.error("Error reading from file '{}'");
            MessageBus.getInstance().send(this, new ErrorStringMessage("Error reading from file '" + _reader.getFileName() + "'\n " + e.getMessage()));
        }
        return stat;
    }

    private void updateLocusStats(final DatabaseStatistics stat, final String currentLocusName, final ArrayList<String> loci, final int numberOfAllelesPerLocus) {
        if (numberOfAllelesPerLocus > 0) {
            loci.add(currentLocusName);

            Integer specimensHavingThisLocus = stat.getSpecimenCountPerLocus().get(currentLocusName);
            if (specimensHavingThisLocus == null) {
                specimensHavingThisLocus = 0;
            }
            stat.getSpecimenCountPerLocus().put(currentLocusName, specimensHavingThisLocus + 1);
        }
    }

    private void checkNumberOfAlleles(final String sampleName, final String currentLocusName, final int numberOfAllelesPerLocus, final ArrayList<String> alleleValues) {
        // Check if the fields are all valid alleles and that the record has no more than 2 alleles
        if (numberOfAllelesPerLocus > 2) {
            _listener.onProblem(sampleName, currentLocusName, "Locus excluded as it has " + numberOfAllelesPerLocus + " alleles: " + alleleValues);
        }
    }
}
