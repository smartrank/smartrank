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
package nl.minvenj.nfi.smartrank.model.smartrank;

import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.LocusLikelihoods;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.PercentReadyMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

/**
 * Collects the results of a set of likelihood calculation jobs for a
 * hypothesis.
 */
public class ResultsCollector extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(ResultsCollector.class);
    private final Collection<Future<LocusProbability>> _futures;
    private final LocusLikelihoods _likelihoods;
    private boolean _interrupted;
    private final boolean _reportProgress;

    /**
     * Constructor.
     *
     * @param futures a collection of Futures representing the calculation jobs.
     */
    public ResultsCollector(final Collection<Future<LocusProbability>> futures, final boolean reportProgress) {
        _futures = futures;
        _reportProgress = reportProgress;
        _likelihoods = new LocusLikelihoods();
        setName("ResultsCollector");
    }

    /**
     * Constructor.
     *
     * @param futures a collection of Futures representing the calculation jobs.
     */
    public ResultsCollector(final Collection<Future<LocusProbability>> futures) {
        this(futures, false);
    }

    @Override
    public void run() {
        try {
            int idx = 0;
            for (final Future<LocusProbability> f : _futures) {
                final LocusProbability prob = f.get();
                Double current = _likelihoods.getLocusProbability(prob.getLocusName());
                if (current == null) {
                    current = prob.getValue();
                }
                else {
                    current += prob.getValue();
                }
                if (_reportProgress) {
                    MessageBus.getInstance().send(this, new PercentReadyMessage((idx++ * 100) / _futures.size()));
                }
                _likelihoods.addLocusProbability(prob.getLocusName(), current);

            }
            LOG.debug("Done! " + _likelihoods);
        }
        catch (final CancellationException ce) {
            // Ignored
            LOG.debug("Analysis was cancelled", ce);
        }
        catch (final Throwable t) {
            if (!_interrupted) {
                LOG.error("Analysis encountered an error", t);
                MessageBus.getInstance().send(this, new ErrorStringMessage(t.getMessage()));
            }
        }
    }

    @Override
    public void interrupt() {
        _interrupted = true;
        for (final Future<LocusProbability> f : _futures) {
            f.cancel(true);
        }
        super.interrupt();
    }

    public LocusLikelihoods getLikelihoods() {
        return _likelihoods;
    }
}
