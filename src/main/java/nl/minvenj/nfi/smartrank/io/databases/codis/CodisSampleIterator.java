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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.analysis.ExcludedProfile;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.io.CSVReader;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.messages.data.EnabledLociMessage;
import nl.minvenj.nfi.smartrank.messages.data.ProsecutionHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.status.DetailStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.PercentReadyMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

class CodisSampleIterator implements Iterator<Sample> {

    private static final Logger LOG = LoggerFactory.getLogger(CodisSampleIterator.class);

    private final CSVReader _reader;
    private final String[] _headers;
    private final Collection<String> _enabledLoci;
    private Sample _nextSample;
    private final List<ExcludedProfile> _badRecords;
    private final ExecutorService _pool;
    private final ArrayList<Future<CodisRecord>> _futures;
    private long _lastAccessTime;
    private final int _minimumNumberOfLoci;
    private final Collection<Sample> _enabledCrimesceneProfiles;
    private final boolean _dropoutAllowed;
    private final int _dbRecordCount;

    private long _specimenIndex;

    public CodisSampleIterator(final CSVReader csvReader, final int recordCount, final List<ExcludedProfile> badRecords) throws IOException {
        _reader = csvReader;
        _badRecords = badRecords;

        // read headers
        _headers = _reader.readFields();
        for (int headerIdx = 0; headerIdx < _headers.length; headerIdx++) {
            _headers[headerIdx] = _headers[headerIdx].replaceFirst("_[1234]$", "").toUpperCase();
        }

        _enabledLoci = MessageBus.getInstance().query(EnabledLociMessage.class);
        _minimumNumberOfLoci = SmartRankRestrictions.getMinimumNumberOfLoci();
        final AnalysisParameters params = MessageBus.getInstance().query(AnalysisParametersMessage.class);
        _enabledCrimesceneProfiles = params.getEnabledCrimesceneProfiles();
        final ProsecutionHypothesis prosecutionHypothesis = MessageBus.getInstance().query(ProsecutionHypothesisMessage.class);
        _dropoutAllowed = prosecutionHypothesis.getCandidateDropout() > 0;
        _dbRecordCount = recordCount;

        _pool = Executors.newFixedThreadPool(8);
        _futures = new ArrayList<>();
        for (int idx = 0; idx < 8; idx++) {
            _futures.add(_pool.submit(new CodisRecordReader(_reader, _headers, _enabledLoci, _enabledCrimesceneProfiles, _minimumNumberOfLoci, _dropoutAllowed)));
        }

        _lastAccessTime = System.currentTimeMillis();
        _specimenIndex = 0;

        final Thread watchDog = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted() && !_pool.isShutdown() && (System.currentTimeMillis() - _lastAccessTime < 30000)) {
                }
                if (!_pool.isShutdown()) {
                    LOG.info("Shutting down pool due to timeout");
                    _pool.shutdown();
                    _futures.clear();
                }
            };
        };

        watchDog.start();
    }

    @Override
    public boolean hasNext() {
        _lastAccessTime = System.currentTimeMillis();
        try {
            while (_nextSample == null && !_futures.isEmpty()) {
                final CodisRecord codisRecord = _futures.remove(0).get();

                if (!codisRecord.isEndOfFile()) {
                    _futures.add(_pool.submit(new CodisRecordReader(_reader, _headers, _enabledLoci, _enabledCrimesceneProfiles, _minimumNumberOfLoci, _dropoutAllowed)));

                    final int percentReady = (int) ((_specimenIndex++ * 100) / _dbRecordCount);
                    MessageBus.getInstance().send(this, new PercentReadyMessage(percentReady));

                    MessageBus.getInstance().send(this, new DetailStringMessage(codisRecord.getSample().getName()));

                    if (codisRecord.isSuccess()) {
                        _nextSample = codisRecord.getSample();
                    }
                    else {
                        _badRecords.add(new ExcludedProfile(codisRecord.getSample(), codisRecord.getExclusionReason()));
                    }
                }
                _lastAccessTime = System.currentTimeMillis();
            }
        }
        catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
        if (_nextSample != null)
            return true;
        LOG.info("Shutting down pool due to last record reached");
        _pool.shutdown();
        return false;
    }

    @Override
    public Sample next() {
        _lastAccessTime = System.currentTimeMillis();
        final Sample next = _nextSample;
        _nextSample = null;
        return next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported.");
    }
}
