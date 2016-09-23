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
package nl.minvenj.nfi.smartrank.io.databases.jdbc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import nl.minvenj.nfi.smartrank.analysis.ExcludedProfile;
import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseReader;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseValidationEventListener;
import nl.minvenj.nfi.smartrank.io.databases.RecordValidator;
import nl.minvenj.nfi.smartrank.messages.status.DetailStringMessage;
import nl.minvenj.nfi.smartrank.raven.NullUtils;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class JDBCReader implements DatabaseReader {

    private final DatabaseConfiguration _config;
    private final List<ExcludedProfile> _badRecordList;
    private final List<Integer> _specimenCountPerNumberOfLoci;
    private final Map<String, Integer> _specimenCountPerLocus;
    private int _recordCount;

    public JDBCReader(final DatabaseConfiguration config) throws MalformedURLException {
        _config = config;
        _badRecordList = new ArrayList<>();
        _specimenCountPerNumberOfLoci = new ArrayList<>();
        _specimenCountPerLocus = new HashMap<>();
    }

    @Override
    public Iterator<Sample> iterator() {
        return new JDBCSampleIterator(this);
    }

    @Override
    public void validate(final DatabaseValidationEventListener listener) throws IOException, InterruptedException {
        MessageBus.getInstance().send(this, new DetailStringMessage("Connecting to database"));
        try (final Connection connection = DriverManager.getConnection(_config.getConnectString(), _config.getUserName(), _config.getPassword())) {
            connection.setReadOnly(true);
            connection.setAutoCommit(false);
            final Statement statement = connection.createStatement();

            long rowCount = 0;
            if (_config.getResultSizeQuery() != null) {
                MessageBus.getInstance().send(this, new DetailStringMessage("Counting results"));
                final ResultSet countResult = statement.executeQuery(_config.getResultSizeQuery());
                if (countResult.next()) {
                    rowCount = countResult.getLong(1);
                }
            }

            MessageBus.getInstance().send(this, new DetailStringMessage("Executing database query"));
            if (statement.execute(_config.getQuery())) {
                final ResultSet resultSet = statement.getResultSet();

                MessageBus.getInstance().send(this, new DetailStringMessage(null));

                int recordNumber = 0;
                final BlockingQueue<JDBCRecordData> validationJobs = new ArrayBlockingQueue<>(200);
                final JDBCRecordValidator[] validators = new JDBCRecordValidator[Runtime.getRuntime().availableProcessors()];
                for (int idx = 0; idx < validators.length; idx++) {
                    validators[idx] = new JDBCRecordValidator(validationJobs, _badRecordList, listener);
                    validators[idx].start();
                }

                Sample sample;
                while ((sample = getNextValidSample(resultSet, true)) != null) {
                    validationJobs.put(new JDBCRecordData(recordNumber++, sample));
                    listener.onProgress(resultSet.getRow(), rowCount);
                }

                for (final JDBCRecordValidator validator : validators) {
                    validator.interrupt();
                    final List<Integer> counts = validator.getSpecimenCountPerNumberOfLoci();
                    while (_specimenCountPerNumberOfLoci.size() < counts.size() + 1) {
                        _specimenCountPerNumberOfLoci.add(new Integer(0));
                    }
                    for (int idx = 0; idx < counts.size(); idx++) {
                        final Integer curCount = _specimenCountPerNumberOfLoci.get(idx);
                        final Integer addCount = counts.get(idx);

                        _specimenCountPerNumberOfLoci.set(idx, curCount + addCount);
                    }
                    final Map<String, Integer> specimenCountPerLocusForThisValidator = validator.getSpecimenCountPerLocus();
                    for (final String locusName : specimenCountPerLocusForThisValidator.keySet()) {
                        Integer locusCount = _specimenCountPerLocus.get(locusName);
                        if (locusCount == null) {
                            locusCount = 0;
                        }
                        _specimenCountPerLocus.put(locusName, locusCount + specimenCountPerLocusForThisValidator.get(locusName));
                    }
                }
                _recordCount = recordNumber;
            }
        }
        catch (final SQLException e) {
            throw new IOException(e);
        }
        finally {
            MessageBus.getInstance().send(this, new DetailStringMessage(""));
        }
    }

    public ResultSet getResultSet() throws SQLException {
        final Connection connection = DriverManager.getConnection(_config.getConnectString(), _config.getUserName(), _config.getPassword());
        connection.setReadOnly(true);
        connection.setAutoCommit(false);
        final Statement statement = connection.createStatement();
        return statement.executeQuery(_config.getQuery());
    }

    public Sample readSample(final ResultSet resultSet) throws SQLException {
        final Sample sample = getNextValidSample(resultSet, false);

        if (sample != null) {
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
        return sample;
    }

    private Sample getNextValidSample(final ResultSet resultSet, final boolean validationMode) throws SQLException {
        Sample sample = null;
        while (!resultSet.isAfterLast() && isBadRecord(sample)) {
            sample = null;
            if (_config.isSingleRowQuery())
                sample = readSampleFromSingleRow(resultSet, validationMode);
            else
                sample = readSampleFromMultipleRows(resultSet, validationMode);
        }
        return sample;
    }

    private boolean isBadRecord(final Sample sample) {
        if (sample == null)
            return true;

        for (final ExcludedProfile profile : _badRecordList) {
            if (profile.getSampleName().equalsIgnoreCase(sample.getName())) {
                return true;
            }
        }
        return false;
    }

    private Sample readSampleFromSingleRow(final ResultSet resultSet, final boolean validationMode) throws SQLException {
        if (resultSet.isAfterLast())
            return null;

        if (resultSet.isBeforeFirst() && !resultSet.next())
            throw new IllegalArgumentException("could not do next");

        final Sample sample = new Sample(resultSet.getString(1).trim());

        int idx = 2;
        while (idx < resultSet.getMetaData().getColumnCount()) {
            final String allele = NullUtils.getValue(resultSet.getString(idx), "NULL").trim();
            if (!allele.equalsIgnoreCase("NULL")) {
                final String locusName = resultSet.getMetaData().getColumnName(idx).replaceFirst("_[1234]$", "").toUpperCase();
                Locus locus = sample.getLocus(locusName);
                if (locus == null) {
                    locus = new Locus(locusName);
                    sample.addLocus(locus);
                }
                if (!validationMode || allele.matches(RecordValidator.VALID_ALLELE_REGEX)) {
                    locus.addAllele(new Allele(allele));
                }
            }
            idx++;
        }
        resultSet.next();
        return sample;
    }

    private Sample readSampleFromMultipleRows(final ResultSet resultSet, final boolean validationMode) throws SQLException {
        if (resultSet.isAfterLast())
            return null;

        if (resultSet.isBeforeFirst() && !resultSet.next()) {
            throw new IllegalArgumentException("could not do next");
        }

        final Sample sample = new Sample(resultSet.getString(_config.getSpecimenIdColumnIndex()).trim());

        while (!resultSet.isAfterLast() && resultSet.getString(_config.getSpecimenIdColumnIndex()).equalsIgnoreCase(sample.getName())) {
            final String locusName = resultSet.getString(_config.getLocusColumnIndex());
            Locus locus = sample.getLocus(Locus.normalize(locusName));
            if (locus == null) {
                locus = new Locus(locusName);
                sample.addLocus(locus);
            }

            final String alleles = resultSet.getString(_config.getAlleleColumnIndex());
            final String[] splitAlleles = alleles.split(" ");
            for (final String allele : splitAlleles) {
                if (!validationMode || allele.matches(RecordValidator.VALID_ALLELE_REGEX)) {
                    locus.addAllele(new Allele(allele));
                }
            }
            resultSet.next();
        }
        return sample;
    }

    @Override
    public int getRecordCount() {
        return _recordCount;
    }

    @Override
    public String getContentHash() {
        return "";
    }

    @Override
    public String getFormatName() {
        return _config.getDatabaseType();
    }

    @Override
    public List<ExcludedProfile> getBadRecordList() {
        return _badRecordList;
    }

    @Override
    public List<Integer> getSpecimenCountPerNumberOfLoci() {
        return _specimenCountPerNumberOfLoci;
    }

    @Override
    public Map<String, Integer> getSpecimenCountsPerLocus() {
        return _specimenCountPerLocus;
    }

}
