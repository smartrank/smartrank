/**
 * Copyright (C) 2013-2015 Netherlands Forensic Institute
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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Contributor;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.Locus;

/**
 * A job that performs dropout estimation for a single hypothesis, generating random profiles and applying dropin and
 */
public class DropoutEstimationJob implements Callable<ArrayList<BigDecimal>> {

    private final Collection<String> _enabledLoci;
    private final int[] _result;
    private final Hypothesis _hypothesis;
    private final int _observedAlleleCount;
    private final DropoutProgressListener _progressListener;

    /**
     * Constructor.
     *
     * @param result an array that will receive the number of surviving alleles per iteration
     * @param hypo the hypothesis for which to perform the estimation
     * @param enabledLoci a collection containing the names of the enabled loci
     * @param observedAlleleCount the number of observed alleles in the crime sample(s)
     * @param progress a {@link DropoutProgressListener} to report progress
     */
    DropoutEstimationJob(final int[] result, final Hypothesis hypo, final Collection<String> enabledLoci, final int observedAlleleCount, final DropoutProgressListener progress) {
        _result = result;
        _hypothesis = hypo;
        _enabledLoci = enabledLoci;
        _observedAlleleCount = observedAlleleCount;
        _progressListener = progress;
    }

    @Override
    public ArrayList<BigDecimal> call() throws Exception {
        final ArrayList<Contributor> mixture = new ArrayList<>();
        final SecureRandom rnd = new SecureRandom();
        rnd.setSeed(System.currentTimeMillis());
        final RandomProfileGenerator randomDudeGenerator = new RandomProfileGenerator(_enabledLoci, _hypothesis.getPopulationStatistics(), rnd);
        final ArrayList<BigDecimal> succesfulDropouts = new ArrayList<>();

        // Simulate a mixture by adding a random profile for each unknown
        for (int randomDude = 0; randomDude < _hypothesis.getUnknownCount(); randomDude++) {
            try {
                final Contributor randomContributor = new Contributor(randomDudeGenerator.getRandomSample(), 0);
                mixture.add(randomContributor);
            } catch (final NoSuchAlgorithmException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        // Add contributors to the mixture
        for (final Contributor contributor : _hypothesis.getContributors()) {
            mixture.add(new Contributor(contributor));
        }

        // Vary dropout probability for persons of interest and unknowns
        for (int step = 0; step < 100; step++) {
            final BigDecimal dropout = new BigDecimal(step).divide(new BigDecimal(100));
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            final ArrayList<String> survivingAlleles = new ArrayList<>();

            // Set the dropout for persons of interest and random dudes
            for (final Contributor contributor : mixture) {
                contributor.setDropoutProbability(dropout.doubleValue());
            }

            applyDropout(rnd, mixture, survivingAlleles);

            // Apply dropIn probability
            if (_hypothesis.getDropInProbability() > 0) {
                applyDropin(rnd, randomDudeGenerator, survivingAlleles);
            }

            // Store surviving alleles
            _result[step] = survivingAlleles.size();

            // If surviving alleles count matches the observed count, record the current dropout value
            final int simulatedAlleleCount = survivingAlleles.size();
            if (simulatedAlleleCount == _observedAlleleCount) {
                succesfulDropouts.add(dropout);
            }
        }
        if (_progressListener != null) {
            _progressListener.onIterationDone();
        }
        return succesfulDropouts;
    }

    /**
     * Applies dropout to the alleles in a mixture.
     *
     * @param rnd a {@link SecureRandom} class to generate randomness for dropout
     * @param mixture A {@link Collection} of {@link Contributor}s representing the samples on which to apply dropout
     * @param survivingAlleles an {@link ArrayList} that will receive the surviving alleles
     * @throws InterruptedException if the analysis is aborted
     */
    protected void applyDropout(final SecureRandom rnd, final ArrayList<Contributor> mixture, final ArrayList<String> survivingAlleles) throws InterruptedException {
        // Apply dropout probability to all alleles in the active loci
        for (final Contributor contributor : mixture) {
            for (final String locusName : _enabledLoci) {
                final Locus locus = contributor.getSample().getLocus(locusName);
                if (locus != null) {
                    for (final Allele allele : locus.getAlleles()) {
                        if (rnd.nextDouble() > contributor.getDropoutProbability() && !survivingAlleles.contains(locus.getName() + "." + allele.getAllele())) {
                            survivingAlleles.add(locus.getName() + "." + allele.getAllele());
                            if (Thread.interrupted()) {
                                throw new InterruptedException();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Applies dropin to a mixture.
     *
     * @param rnd a {@link SecureRandom} to supply randomness
     * @param randomDudeGenerator a {@link RandomProfileGenerator} that supplies random alleles
     * @param survivingAlleles an {@link ArrayList} that contains the surviving alleles after dropout. The dropped in allele (if any) will be added to this list.
     */
    protected void applyDropin(final SecureRandom rnd, final RandomProfileGenerator randomDudeGenerator, final ArrayList<String> survivingAlleles) {
        for (final String locusName : _enabledLoci) {
            if (rnd.nextDouble() < _hypothesis.getDropInProbability()) {
                final String dropinAllele = locusName + "." + randomDudeGenerator.getRandomAllele(locusName).getAllele();
                if (!survivingAlleles.contains(dropinAllele)) {
                    final String droppedIn = locusName + "." + randomDudeGenerator.getRandomAllele(locusName).getAllele();
                    survivingAlleles.add(droppedIn);
                }
            }
        }
    }
}
