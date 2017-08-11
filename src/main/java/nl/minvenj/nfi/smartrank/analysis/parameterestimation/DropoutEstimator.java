/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.smartrank.analysis.parameterestimation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.messages.status.DetailStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.PercentReadyMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

/**
 * Performs estimation of dropout on a hypothesis. the class extends {@link Thread} to allow the estimations to run in parallel.
 */
public class DropoutEstimator extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(DropoutEstimator.class);
    private final int _iterations;
    private final DropoutProgressListener _progressListener;
    private final ExecutorService _service;
    private final MessageBus _messageBus;

    /**
     * Constructor.
     *
     * @param progress a {@link DropoutProgressListener} to report progress of the estimation
     */
    public DropoutEstimator(final DropoutProgressListener progress) {
        LOG.trace("DropoutEstimator");
        setName("Dropout Estimator");
        _progressListener = progress;
        _messageBus = MessageBus.getInstance();
        _iterations = SmartRankRestrictions.getParameterEstimationIterations();
        if (_progressListener != null) {
            _progressListener.setIterations(_iterations);
        }
        _service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Performs estimation of dropout.
     *
     * @param hypo the {@link Hypothesis} for which to perform dropout estimation
     * @param enabledLoci a {@link Collection} of enabled loci
     * @param crimesceneProfiles a {@link Collection} of crimescene profiles
     * @return a {@link DropoutEstimation} object holding the results of the estimation
     */
    public DropoutEstimation estimate(final Hypothesis hypo, final Collection<String> enabledLoci, final Collection<Sample> crimesceneProfiles) {
        final DropoutEstimation estimate = new DropoutEstimation();
        estimate.setIterations(_iterations);

        // This arraylist is used to store all dropouts at which a monte carlo simulation yields a number of surviving alleles that matches the observed allele count
        final ArrayList<BigDecimal> succesfulDropouts = new ArrayList<>();
        final int[][] results = new int[_iterations][100];
        final ArrayList<Future<ArrayList<BigDecimal>>> futures = new ArrayList<>();

        final int observedAlleleCount = getObservedAlleleCount(enabledLoci, crimesceneProfiles);
        // Perform the dropout estimation using the configured number of iterations
        for (int iteration = 0; iteration < _iterations; iteration++) {
            futures.add(_service.submit(new DropoutEstimationJob(results[iteration], hypo, enabledLoci, observedAlleleCount, _progressListener)));
        }

        _service.shutdown();

        for (int iteration = 0; iteration < _iterations; iteration++) {
            try {
                final Future<ArrayList<BigDecimal>> future = futures.remove(0);
                final ArrayList<BigDecimal> iterationResults = future.get();
                if (!iterationResults.isEmpty()) {
                    LOG.debug("Iteration {} resulted in {} Succesful dropouts: {}", iteration, iterationResults.size(), iterationResults);
                    succesfulDropouts.addAll(iterationResults);
                }
                _messageBus.send(this, new DetailStringMessage(iteration + "/" + _iterations));
                _messageBus.send(this, new PercentReadyMessage(iteration * 100 / _iterations));
            }
            catch (final Throwable t) {
                _service.shutdownNow();
                try {
                    _service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                }
                catch (final InterruptedException ex1) {
                }
                throw new IllegalArgumentException(t);
            }
        }

        _messageBus.send(this, new DetailStringMessage(""));

        // Store the minimum and maximum
        if (!succesfulDropouts.isEmpty()) {
            Collections.sort(succesfulDropouts);
            estimate.setData(succesfulDropouts);
        }
        else {
            throw new IllegalArgumentException("Dropout estimation resulted in no matching attempts. Is the number of donors realistic?");
        }
        return estimate;
    }

    /**
     * @return the number of unique alleles in the active crimescene profiles.
     *         If more than one crimescene profile is loaded, the average allele
     *         count over all active crimescene profiles is returned.
     */
    private int getObservedAlleleCount(final Collection<String> enabledLoci, final Collection<Sample> crimesceneProfiles) {
        // Count the unique alleles in the samples, take the average over all replicates
        int observedAlleleCount = 0;
        for (final Sample replicate : crimesceneProfiles) {
            int alleleCount = 0;
            for (final String locusName : enabledLoci) {
                final Locus locus = replicate.getLocus(locusName);
                if (locus != null) {
                    alleleCount += locus.getAlleles().size();
                }
            }
            LOG.debug("Replicate {} has {} alleles", replicate, alleleCount);
            observedAlleleCount += alleleCount;
        }

        // Take the average over all replicates
        if (!crimesceneProfiles.isEmpty()) {
            LOG.debug(observedAlleleCount + " / " + crimesceneProfiles.size() + " = " + observedAlleleCount / crimesceneProfiles.size());
            observedAlleleCount /= crimesceneProfiles.size();
        }
        return observedAlleleCount;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        _service.shutdownNow();
    }

}
