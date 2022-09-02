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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FilenameUtils;

import nl.minvenj.nfi.smartrank.analysis.ExcludedProfile;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.CSVReader;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseReader;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseStatistics;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseValidationEventListener;

/**
 * Reads a database in Codis format from a file.
 */
public class CodisDatabaseReader implements DatabaseReader, Iterable<Sample> {

    private static final String LOCUS_FIELD_REGEX = "[\\w ]+_\\d+";
    private static final String SPECIMEN_ID_FIELD_REGEX = "specimenId";
    private boolean _validated;
    private String[] _headers;
    private int _recordCount;
    private String _fileHash;
    private final File _file;
    private final List<ExcludedProfile> _badRecordList;
    private final List<Integer> _specimenCountPerNumberOfLoci;
    private final Map<String, Integer> _specimenCountPerLocus;
    private long _lastModifiedTimeWhenValidated;

    public CodisDatabaseReader(final File dbFile) throws FileNotFoundException, MalformedURLException, IOException {
        _file = dbFile;
        _badRecordList = new ArrayList<>();
        _specimenCountPerNumberOfLoci = new ArrayList<>();
        _specimenCountPerLocus = new HashMap<>();
        _lastModifiedTimeWhenValidated = -1L;
    }

    @Override
    public void validate(final DatabaseValidationEventListener listener) throws IOException, InterruptedException {
        final String extension = FilenameUtils.getExtension(_file.getAbsolutePath()).toLowerCase();

        if (!extension.equals("csv") && !extension.equals("txt") && !extension.equals("xml")) {
            throw new IllegalArgumentException("Invalid file format. Only .csv, .txt or .xml formats are supported!");
        }

        if (!_validated) {
            final CSVReader validationReader = new CSVReader(_file);

            validateHeaders(validationReader);
            validateRecords(validationReader, listener);

            _lastModifiedTimeWhenValidated = _file.lastModified();
            _validated = true;
        }
    }

    @Override
    public void revalidate(final DatabaseValidationEventListener listener) throws IOException, InterruptedException {
        if (_file.exists() && _file.lastModified() != _lastModifiedTimeWhenValidated) {
            _validated = false;
            _recordCount = 0;
            _badRecordList.clear();
            _specimenCountPerNumberOfLoci.clear();
            _specimenCountPerLocus.clear();
            validate(listener);
        }
    }

    private void validateHeaders(final CSVReader validationReader) throws IOException, IllegalArgumentException {
        _headers = validationReader.readFields();

        // Do sanity check on the header fields (if any)
        if (_headers.length == 0) {
            throw new IllegalArgumentException("This is not a Codis Database file. No line header found!");
        }

        if (!_headers[0].matches(SPECIMEN_ID_FIELD_REGEX)) {
            if (_headers[0].length() > 60) {
                throw new IllegalArgumentException("This is not a Codis Database file. Invalid header found at index 0");
            }
            else {
                throw new IllegalArgumentException("This is not a Codis Database file. Invalid header found at index 0: " + _headers[0]);
            }
        }

        for (int idx = 1; idx < _headers.length; idx++) {
            if (!_headers[idx].matches(LOCUS_FIELD_REGEX)) {
                throw new IllegalArgumentException("This is not a Codis Database file. Invalid header found at index " + idx + ": " + _headers[idx]);
            }
        }
    }

    @Override
    public int getRecordCount() {
        return _recordCount;
    }

    @Override
    public String getContentHash() {
        return _fileHash;
    }

    @Override
    public String getFormatName() {
        return "CODIS";
    }

    private void validateRecords(final CSVReader validationReader, final DatabaseValidationEventListener listener) throws IOException, InterruptedException {
        final ExecutorService pool = Executors.newCachedThreadPool();
        final ArrayList<Future<DatabaseStatistics>> futures = new ArrayList<>();
        for (int idx = 0; idx < 8; idx++) {
            futures.add(pool.submit(new CodisRecordValidator(_headers, validationReader, listener)));
        }

        final Thread progressReporter = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    listener.onProgress(validationReader.getOffset(), _file.length());
                }
            }
        };

        progressReporter.start();

        for (final Future<DatabaseStatistics> future : futures) {
            try {
                final DatabaseStatistics stat = future.get();
                final List<Integer> counts = stat.getSpecimenCountPerNumberOfLoci();
                while (_specimenCountPerNumberOfLoci.size() < counts.size() + 1) {
                    _specimenCountPerNumberOfLoci.add(new Integer(0));
                }
                for (int idx = 0; idx < counts.size(); idx++) {
                    final Integer curCount = _specimenCountPerNumberOfLoci.get(idx);
                    final Integer addCount = counts.get(idx);

                    _specimenCountPerNumberOfLoci.set(idx, curCount + addCount);
                }
                final Map<String, Integer> specimenCountPerLocusForThisValidator = stat.getSpecimenCountPerLocus();
                for (final String locusName : specimenCountPerLocusForThisValidator.keySet()) {
                    Integer locusCount = _specimenCountPerLocus.get(locusName);
                    if (locusCount == null) {
                        locusCount = 0;
                    }
                    _specimenCountPerLocus.put(locusName, locusCount + specimenCountPerLocusForThisValidator.get(locusName));
                }
                _recordCount += stat.getRecordCount();
            }
            catch (final ExecutionException e) {
                pool.shutdown();
                throw new IOException(e);
            }
        }
        pool.shutdown();
        progressReporter.interrupt();

        _fileHash = validationReader.getFileHash();
    }

    @Override
    public Iterator<Sample> iterator() {
        try {
            _badRecordList.clear();
            return new CodisSampleIterator(new CSVReader(_file), _recordCount, _badRecordList);
        }
        catch (final IOException ex) {
            throw new IllegalArgumentException("Cannot create an iterator for '" + _file + "'", ex);
        }
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
        return new HashMap<>();
    }

    @Override
    public Iterator<Sample> iterator(final Properties properties) {
        return iterator();
    }
}
