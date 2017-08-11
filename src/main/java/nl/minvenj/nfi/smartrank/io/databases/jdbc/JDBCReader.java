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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.analysis.ExcludedProfile;
import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseReader;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseValidationEventListener;
import nl.minvenj.nfi.smartrank.io.databases.codis.CodisRecordValidator;
import nl.minvenj.nfi.smartrank.messages.status.DetailStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.PercentReadyMessage;
import nl.minvenj.nfi.smartrank.raven.NullUtils;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class JDBCReader implements DatabaseReader {

    private static final Logger LOG = LoggerFactory.getLogger(JDBCReader.class);

    private final DatabaseConfiguration _config;
    private final List<ExcludedProfile> _badRecordList;
    private final List<Integer> _specimenCountPerNumberOfLoci;
    private final Map<String, Integer> _specimenCountPerLocus;
    private int _recordCount;
    private Connection _connection;
    private String _contentHash;

    private final Map<String, Map<String, Integer>> _metadataStatistics;

    public JDBCReader(final DatabaseConfiguration config) throws MalformedURLException {
        _config = config;
        _badRecordList = new ArrayList<>();
        _specimenCountPerNumberOfLoci = new ArrayList<>();
        _specimenCountPerLocus = new HashMap<>();
        _metadataStatistics = new HashMap<>();
    }

    @Override
    public Iterator<Sample> iterator() {
        return new JDBCSampleIterator(this);
    }

    @Override
    public void validate(final DatabaseValidationEventListener listener) throws IOException, InterruptedException {
        LOG.info("Validating database");
        MessageBus.getInstance().send(this, new PercentReadyMessage(-1));
        try {
            final JDBCResultSetChunker resultSet = getResultSet();

            _contentHash = resultSet.getContentHash();

            final BlockingQueue<Sample> validationJobs = new ArrayBlockingQueue<>(200);
            final JDBCRecordValidator[] validators = new JDBCRecordValidator[Math.min(8, Runtime.getRuntime().availableProcessors())];
            for (int idx = 0; idx < validators.length; idx++) {
                validators[idx] = new JDBCRecordValidator(validationJobs, _badRecordList, listener);
                validators[idx].start();
            }

            Sample sample;
            while ((sample = getNextValidSample(resultSet, true)) != null) {
                validationJobs.put(sample);
                listener.onProgress(_recordCount++, resultSet.getNumberOfSpecimens());
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
//            LOG.info("Validation complete: {} records out of {} used.", _recordCount, resultSet.getNumberOfSpecimens());
        }
        catch (final Throwable t) {
            throw new IOException(t);
        }
        finally {
            MessageBus.getInstance().send(this, new DetailStringMessage(""));
        }
    }

    @Override
    public void revalidate(final DatabaseValidationEventListener listener) throws IOException {
        try (final JDBCResultSetChunker resultSet = getResultSet()) {
            LOG.info("Checking if revalidation of database is required");

            final String currentContentHash = resultSet.getContentHash();
            if (!currentContentHash.equals(_contentHash)) {
                LOG.info("Revalidation of database is required: current hash {} differs from previous hash {}", currentContentHash, _contentHash);
                MessageBus.getInstance().send(this, new DetailStringMessage("Revalidating database"));
                _recordCount = 0;
                _badRecordList.clear();
                _specimenCountPerLocus.clear();
                _specimenCountPerNumberOfLoci.clear();
                _metadataStatistics.clear();
                validate(listener);
            }
            else {
                LOG.info("Revalidation of database is not required");
            }
        }
        catch (final Throwable t) {
            throw new IOException(t);
        }
        finally {
            MessageBus.getInstance().send(this, new DetailStringMessage(""));
        }
    }

    private void connect() throws SQLException {
        if (disconnectIfInvalid()) {
            MessageBus.getInstance().send(this, new PercentReadyMessage(-1));
            MessageBus.getInstance().send(this, new DetailStringMessage("Connecting to database"));
            LOG.info("Establishing new connection to {}", _config.getConnectString());
            _connection = DriverManager.getConnection(_config.getConnectString(), _config.getProperties());
            _connection.setReadOnly(true);
            _connection.setAutoCommit(false);
        }
    }

    /**
     * Determines if the current connection (if any) represents a valid database connection, and closes the connection if it represents an invalid connection.
     *
     * @return true if the connection was null or invalid and closed by the method, false if the connection object represented a valid connection.
     */
    private boolean disconnectIfInvalid() {
        if (!isConnectionValid()) {
            disconnect();
            return true;
        }
        return false;
    }

    /**
     * Checks to see if the current database connection (if any) is valid.
     *
     * @return true if the current connection is valid, false if there is no connection, or the connection is not valid
     */
    private boolean isConnectionValid() {
        Statement statement = null;

        if (_connection == null) {
            return false;
        }

        try {
            statement = _connection.createStatement();
            statement.setQueryTimeout(2);
            statement.executeQuery(_config.getConnectionTestQuery());
            return true;
        }
        catch (final SQLException e) {
            LOG.warn("Database connection was invalid. Checked using query: {}", _config.getConnectionTestQuery(), e);
            return false;
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (final SQLException e) {
                    // Yeah sure...
                }
            }
        }
    }

    /**
     * Disconnects from the current database connection (if any).
     */
    private void disconnect() {
        if (_connection != null) {
            try {
                _connection.close();
            }
            catch (final SQLException e) {
                // We don't care if the close method fails. We are cleaning up after all....
            }
            _connection = null;
        }
    }

    public JDBCResultSetChunker getResultSet() throws SQLException {
        connect();
        _metadataStatistics.clear();
        return new JDBCResultSetChunker(_connection, _config);
    }

    public Sample readSample(final JDBCResultSetChunker resultSet) throws SQLException {
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

    private Sample getNextValidSample(final JDBCResultSetChunker resultSet, final boolean validationMode) throws SQLException {
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

    private Sample readSampleFromSingleRow(final JDBCResultSetChunker resultSet, final boolean validationMode) throws SQLException {
        if (resultSet.isAfterLast())
            return null;

        if (resultSet.isBeforeFirst() && !resultSet.next())
            throw new IllegalArgumentException("could not do next");

        final Sample sample = new Sample(resultSet.getString(1).trim());

        int idx = 2;
        while (idx < resultSet.getMetaData().getColumnCount()) {
            final String allele = NullUtils.getValue(resultSet.getString(idx), "NULL").trim();
            if (!allele.equalsIgnoreCase("NULL")) {
                final String locusName = Locus.normalize(resultSet.getMetaData().getColumnName(idx).replaceFirst("_[1234]$", ""));
                Locus locus = sample.getLocus(locusName);
                if (locus == null) {
                    locus = new Locus(locusName);
                    sample.addLocus(locus);
                }
                if (!validationMode || allele.matches(CodisRecordValidator.VALID_ALLELE_REGEX)) {
                    locus.addAllele(new Allele(allele));
                }
            }
            idx++;
        }
        resultSet.next();
        return sample;
    }

    private Sample readSampleFromMultipleRows(final JDBCResultSetChunker resultSet, final boolean validationMode) throws SQLException {
        if (resultSet.isAfterLast())
            return null;

        if (resultSet.isBeforeFirst() && !resultSet.next()) {
            throw new IllegalArgumentException("could not do next");
        }

        final SQLWarning warnings = resultSet.getWarnings();
        if (warnings != null) {
            for (final Throwable throwable : warnings) {
                LOG.error("Error: {}", throwable.getMessage());
            }
        }

        final Sample sample = new Sample(resultSet.getString(_config.getSpecimenIdColumnIndex()).trim());

        final StringBuilder additionalData = new StringBuilder();
        final ResultSetMetaData metaData = resultSet.getMetaData();
        for (int idx = 1; !validationMode && idx <= metaData.getColumnCount(); idx++) {
            if (idx != _config.getSpecimenIdColumnIndex() && idx != _config.getLocusColumnIndex() && idx != _config.getAlleleColumnIndex()) {
                if (additionalData.length() != 0)
                    additionalData.append(", ");
                final String metaName = metaData.getColumnLabel(idx);
                final String metaValue = resultSet.getString(idx);
                additionalData.append(metaName).append(": ").append(metaValue);
                addMetadataStatistics(metaName, metaValue);
            }

        }
        sample.setAdditionalData(additionalData.toString());

        LOG.debug("Created Sample '{}'", sample.getName());
        while (!resultSet.isAfterLast() && resultSet.getString(_config.getSpecimenIdColumnIndex()).trim().equalsIgnoreCase(sample.getName())) {
            final String locusName = resultSet.getString(_config.getLocusColumnIndex()).trim();
            Locus locus = sample.getLocus(Locus.normalize(locusName));
            if (locus == null) {
                locus = new Locus(locusName);
                sample.addLocus(locus);
            }

            final String alleles = resultSet.getString(_config.getAlleleColumnIndex()).trim();
            final String[] splitAlleles = alleles.split(" ");
            for (final String allele : splitAlleles) {
                if (!validationMode || allele.matches(CodisRecordValidator.VALID_ALLELE_REGEX)) {
                    locus.addAllele(new Allele(allele));
//                    LOG.debug("  Adding allele {}.{}", locus.getName(), allele);
                }
            }
            resultSet.next();
            if (resultSet.isAfterLast()) {
                LOG.debug("  ResultSet isAfterLast");
            }
            else {
                LOG.debug("  Next specimen ID: {}", resultSet.getString(_config.getSpecimenIdColumnIndex()).trim());
            }
        }
        return sample;
    }

    private void addMetadataStatistics(final String metaName, final String metaValue) {
        Map<String, Integer> map = _metadataStatistics.get(metaName);
        if (map == null) {
            map = new HashMap<>();
            _metadataStatistics.put(metaName, map);
        }
        final Integer count = NullUtils.getValue(map.get(metaValue), new Integer(0));
        map.put(metaValue, count + 1);
    }

    @Override
    public int getRecordCount() {
        return _recordCount;
    }

    @Override
    public String getContentHash() {
        return _contentHash;
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

    @Override
    public Map<String, Map<String, Integer>> getMetadataStatistics() {
        return _metadataStatistics;
    }

    public void close() {
        try {
            _connection.close();
        }
        catch (final SQLException e) {
        }
    }
}
