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
package nl.minvenj.nfi.smartrank.domain;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.minvenj.nfi.smartrank.analysis.ExcludedProfile;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseReader;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseReaderFactory;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseValidationEventListener;

/**
 * Represents the DNA database.
 */
public class DNADatabase {

    private static final int CACHE_SIZE = SmartRankRestrictions.getDBCacheSize();
    private final DatabaseConfiguration _configuration;
    private DatabaseReader _reader;

    public DNADatabase(final File dbFile) {
        _configuration = new DatabaseConfiguration(dbFile);
    }

    public DNADatabase(final DatabaseConfiguration config) {
        _configuration = config;
    }

    public void validate(final DatabaseValidationEventListener listener) throws IOException, InterruptedException {
        _reader = DatabaseReaderFactory.create(_configuration);
        _reader.validate(listener);
    }

    public String getConnectString() {
        return _configuration.getConnectString();
    }

    public DatabaseConfiguration getConfiguration() {
        return _configuration;
    }

    public int getRecordCount() {
        return _reader.getRecordCount();
    }

    public List<Integer> getSpecimenCountPerNumberOfLoci() {
        return _reader.getSpecimenCountPerNumberOfLoci();
    }

    public String getFileHash() {
        return _reader.getContentHash();
    }

    public String getFormatName() {
        return _reader.getFormatName();
    }

    public Iterator<Sample> iterator() {
        return new CachedSampleIterator();
    }

    private class CachedSampleIterator implements Iterator<Sample> {

        private Iterator<Sample> _iterator;
        private final Sample[] _profileCache = new Sample[CACHE_SIZE];
        private int _cacheHead = 0;
        private int _cacheTail = 0;

        @Override
        public boolean hasNext() {
            if (_cacheHead >= _cacheTail) {
                fillProfileCache();
            }
            return _cacheHead < _cacheTail;
        }

        @Override
        public Sample next() {
            return _profileCache[_cacheHead++];
        }

        private void fillProfileCache() {
            if (_iterator == null) {
                _iterator = _reader.iterator();
            }
            _cacheTail = 0;
            _cacheHead = 0;
            while (_cacheTail < _profileCache.length && _iterator.hasNext()) {
                _profileCache[_cacheTail++] = _iterator.next();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove is not supported.");
        }
    }

    public List<ExcludedProfile> getBadRecordList() {
        return _reader.getBadRecordList();
    }

    public Map<String, Integer> getSpecimenCountsPerLocus() {
        return _reader.getSpecimenCountsPerLocus();
    }
}
