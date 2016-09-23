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
package nl.minvenj.nfi.smartrank.model.smartrank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Contributor;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.model.smartrank.genotype.GenotypeProbabilityCalculator;
import nl.minvenj.nfi.smartrank.model.smartrank.genotype.GenotypeProbabilityCalculatorFactory;

/**
 * This class performs probability calculations at a single locus for a single
 * hypothesis.
 */
public class LocusProbabilityJob implements Callable<LocusProbability> {

    private static final Logger LOG = LoggerFactory.getLogger(LocusProbabilityJob.class);

    private final Hypothesis _hypothesis;
    private final LocusProbability _locusProbability;
    private final PermutationIterator _permutationIterator;
    private final Collection<Locus> _replicateLoci;
    private final String _locusName;
    // Set A is the drop-out set. These alleles are present in the reference sample, but not the replicate
    private final Allele[] _droppedOutA = new Allele[Allele.getRegisteredAlleleCount()];
    private int _droppedOutACount = 0;
    // This array holds the product of all dropout probabilitities for all alleles that are present in one or more profile
    private final double[] _presentBProbabilities = new double[Allele.getRegisteredAlleleCount()];
    // Set C is the set of of alleles that are present in the replicate, but not the reference sample
    private final Allele[] _droppedInC = new Allele[Allele.getRegisteredAlleleCount()];
    private int _droppedInCCount = 0;

    private int _presentMinId;
    private int _presentMaxId;

    // This array contains all alleles for all contributors augmented with the alleles for the current permutation
    private final Allele[] _allAlleles = new Allele[Allele.getRegisteredAlleleCount() * 2];
    private int _allAlleleCount = 0;

// Cache for locus results. This to be able to skip recalculation for identical replicate loci
    private final HashMap<Locus, Double> _locusProbabilities;

    private final double _dropOutProbability;
    private final double _dropOutProbabilityHomozygote;
    private final double _presentMultipleProbability;
    private final double _presentMultipleProbabilityHomozygote;
    private double _denominator;

    private int _totalAlleleCount;
    private final int[] _alleleCounts = new int[Allele.getRegisteredAlleleCount()];
    private final int[] _localAlleleCounts = new int[Allele.getRegisteredAlleleCount()];
    private int _currentAlleleCount;
    private final GenotypeProbabilityCalculator _genotypeCalculator;

    private boolean _interrupted;

    LocusProbabilityJob(final String locusName, final Collection<Sample> crimesceneProfiles, final Hypothesis hypothesis) {
        this(locusName, null, crimesceneProfiles, hypothesis);
    }

    LocusProbabilityJob(final String locusName, final PermutationIterator permutationIterator, final Collection<Sample> replicates, final Hypothesis hypothesis) {
        if (locusName == null) {
            throw new IllegalArgumentException("No locus name specified!");
        }

        if (replicates == null || replicates.isEmpty()) {
            throw new IllegalArgumentException("No replicates specified!");
        }

        _locusProbabilities = new HashMap<>(replicates.size());
        _permutationIterator = permutationIterator;
        _replicateLoci = new ArrayList<>();
        for (final Sample replicate : replicates) {
            Locus locus = replicate.getLocus(locusName);
            if (locus == null) {
                locus = new Locus(locusName);
                locus.setSample(replicate);
            }
            _replicateLoci.add(locus);
        }
        _hypothesis = hypothesis;
        _locusName = locusName;
        _locusProbability = new LocusProbability(hypothesis, locusName);

        _dropOutProbability = hypothesis.getUnknownDropoutProbability();
        _dropOutProbabilityHomozygote = _dropOutProbability * _dropOutProbability;
        _presentMultipleProbability = _dropOutProbability;
        _presentMultipleProbabilityHomozygote = _dropOutProbabilityHomozygote;

        _genotypeCalculator = GenotypeProbabilityCalculatorFactory.getUnrelatedGenotypeProbabilityCalculator(hypothesis);

        // Build allele count table
        for (final Contributor contributor : hypothesis.getContributors()) {
            Locus locus = contributor.getSample().getLocus(locusName);

            if (locus == null) {
                locus = new Locus(locusName);
            }

            boolean skipHomozygotes = false;
            for (final Allele allele : locus.getAlleles()) {
                _alleleCounts[allele.getId()]++;
                _totalAlleleCount++;
                if (!skipHomozygotes) {
                    _allAlleles[_allAlleleCount++] = allele;
                    skipHomozygotes = allele.isHomozygote();
                }
            }
        }
        for (final Contributor nonContributor : hypothesis.getNonContributors()) {
            Locus locus = nonContributor.getSample().getLocus(locusName);

            if (locus == null) {
                locus = new Locus(locusName);
//                throw new IllegalArgumentException("Input error: profile " + nonContributor.getSample().getName() + " does not contain locus " + locusName);
            }
            for (final Allele allele : locus.getAlleles()) {
                _alleleCounts[allele.getId()]++;
                _totalAlleleCount++;
            }
        }

        _denominator = 1;
        if (hypothesis.getThetaCorrection() > 0) {
            for (int i = _totalAlleleCount; i < _totalAlleleCount + hypothesis.getUnknownCount() * 2; i++) {
                _denominator *= (1 + (i - 1) * hypothesis.getThetaCorrection());
            }
        }
    }

    @Override
    public LocusProbability call() throws Exception {
        LOG.debug("Started {}", _locusName);
        try {
            if (_permutationIterator == null) {
                _locusProbability.setValue(calculateSingleLocusProbability());
            }
            else {
                Permutation permutation;
                while ((permutation = _permutationIterator.next()) != null) {
                    if (_interrupted)
                        return _locusProbability;

                    final double replicateProbability = calculateReplicateProbability(permutation.getLoci());
                    final double genotypeProbability = calculateGenotypeProbability(permutation.getLoci());
                    final double prob = genotypeProbability * replicateProbability;
                    _locusProbability.addValue(permutation.getPermutationFactor() * prob);
                }

            }
        }
        catch (final NoSuchElementException e) {
            // Can happen if multiple thread share a permutation iterator object. Nothing to worry about.
            LOG.debug("No Such Element", e);
        }
//        catch (final InterruptedException ie) {
//            LOG.debug("Calculations for locus {} were interrupted!", _locusName);
//            throw ie;
//        }
        catch (final Exception e) {
            throw new Exception("Error in Locus Probability calculation for " + _locusName, e);
        }

        return _locusProbability;
    }

    public double calculateSingleLocusProbability() {
        return calculateReplicateProbability(new Locus[]{});
    }

    public double calculateReplicateProbability(final Locus[] unknowns) {
        _locusProbabilities.clear();
        double replicateProbability = 1;

        _currentAlleleCount = _allAlleleCount;
        if (unknowns != null) {
            for (final Locus unknown : unknowns) {
                boolean skipHomozygotes = false;
                for (final Allele allele : unknown.getAlleles()) {
                    if (!skipHomozygotes) {
                        _allAlleles[_currentAlleleCount++] = allele;
                        skipHomozygotes = allele.isHomozygote();
                    }
                }
            }
        }

        for (final Locus replicateLocus : _replicateLoci) {
            Double precalculated = _locusProbabilities.get(replicateLocus);
            if (precalculated == null) {
                precalculated = calculateReplicateProbability(replicateLocus);
                _locusProbabilities.put(replicateLocus, precalculated);
            }
            replicateProbability *= precalculated;
        }
        return replicateProbability;
    }

    private double calculateReplicateProbability(final Locus replicateLocus) {
        double returnValue = 1.0;

        final boolean[] presentFlags = new boolean[Allele.getRegisteredAlleleCount()];

        classifyAlleles(replicateLocus, presentFlags);

        for (int idx = 0; idx < _droppedOutACount; idx++) {
            final Allele a = _droppedOutA[idx];
            if (a.getLocus().getSample() == null) {
                returnValue *= a.isHomozygote() ? _dropOutProbabilityHomozygote : _dropOutProbability;
            }
            else {
                returnValue *= _hypothesis.getContributor(a).getDropOutProbability(a.isHomozygote());
            }
        }

        for (int idx = _presentMinId; idx <= _presentMaxId; idx++) {
            if (presentFlags[idx]) {
                returnValue *= 1 - _presentBProbabilities[idx];
            }
        }

        if (_droppedInCCount == 0) {
            returnValue *= 1 - _hypothesis.getDropInProbability();
        }
        else {
            for (int idx = 0; idx < _droppedInCCount; idx++) {
                final Allele a = _droppedInC[idx];
                returnValue *= _hypothesis.getDropInProbability() * _hypothesis.getPopulationStatistics().getProbability(replicateLocus, a);
            }
        }

        return returnValue;
    }

    private void classifyAlleles(final Locus replicateLocus, final boolean[] presentFlags) {
        _droppedOutACount = 0;
        _droppedInCCount = 0;

        _presentMinId = _presentBProbabilities.length - 1;
        _presentMaxId = 0;

        // Assume all alleles have dropped in. We will move the alleles out of this set as appropriate.
        // Note that alleles from homozygote loci only get added once
        for (final Allele allele : replicateLocus.getAlleles()) {
            if (!contains(_droppedInC, _droppedInCCount, allele)) {
                _droppedInC[_droppedInCCount++] = allele;
            }
            _presentBProbabilities[allele.getId()] = 1.0;
            presentFlags[allele.getId()] = false;
            if (allele.getId() < _presentMinId) {
                _presentMinId = allele.getId();
            }
            if (allele.getId() > _presentMaxId) {
                _presentMaxId = allele.getId();
            }
        }

        for (int alleleIndex = 0; alleleIndex < _currentAlleleCount; alleleIndex++) {
            final Allele allele = _allAlleles[alleleIndex];
            // If this allele is not present in the replicate, it goes into set A
            if (!replicateLocus.getAlleles().contains(allele)) {
                _droppedOutA[_droppedOutACount++] = allele;
            }
            else {
                // Remove this allele from the Dropped In set C
                final int cIndex = indexOf(_droppedInC, _droppedInCCount, allele);
                if (cIndex >= 0) {
                    remove(_droppedInC, _droppedInCCount--, cIndex);
                }

                presentFlags[allele.getId()] = true;

                // Update the product of dropout probabilities for this allele
                if (allele.getLocus().getSample() == null) {
                    _presentBProbabilities[allele.getId()] *= allele.isHomozygote() ? _presentMultipleProbabilityHomozygote : _presentMultipleProbability;
                }
                else {
                    _presentBProbabilities[allele.getId()] *= _hypothesis.getContributor(allele).getDropOutProbability(allele.isHomozygote());
                }
            }
        }
    }

    private Allele remove(final Allele[] array, final int size, final int index) {
        final Allele a = array[index];
        array[index] = array[size - 1];
        return a;
    }

    private int indexOf(final Allele[] array, final int size, final Allele allele) {
        for (int idx = 0; idx < size; idx++) {
            if (array[idx].getId() == allele.getId()) {
                return idx;
            }
        }
        return -1;
    }

    private boolean contains(final Allele[] array, final int size, final Allele allele) {
        for (int idx = 0; idx < size; idx++) {
            if (array[idx].getId() == allele.getId()) {
                return true;
            }
        }
        return false;
    }

    public double calculateGenotypeProbability(final Locus[] genotypeSetForUnknowns) {
        double genotypeProbability = 1;
        System.arraycopy(_alleleCounts, 0, _localAlleleCounts, 0, _alleleCounts.length);

        // Calculate the genotype probability for the first unknown contributor
        for (int idx = 0; idx < genotypeSetForUnknowns.length; idx++) {
            genotypeProbability *= _genotypeCalculator.calculate(_localAlleleCounts, genotypeSetForUnknowns[idx]);
        }

        return genotypeProbability / _denominator;
    }

    public void interrupt() {
        _interrupted = true;
    }
}
