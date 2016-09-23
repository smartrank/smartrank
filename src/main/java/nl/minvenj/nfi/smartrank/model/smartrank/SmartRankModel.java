/**
 * Copyright (C) 2013-2105 Netherlands Forensic Institute
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.Contributor;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.LocusLikelihoods;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.messages.data.EnabledLociMessage;
import nl.minvenj.nfi.smartrank.model.StatisticalModel;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public final class SmartRankModel implements StatisticalModel {

    private static final Logger LOG = LoggerFactory.getLogger(SmartRankModel.class);
    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private ResultsCollector _resultsCollector;
    private final Locus[][] _possibleAlleleCombinationsPerLocus = new Locus[Locus.getRegisteredLocusCount()][];
    private final double[][][] _resultCache;

    public SmartRankModel() {
        _resultCache = new double[Locus.getRegisteredLocusCount() + 1][500][500];
    }

    @Override
    public LocusLikelihoods calculateLikelihood(final Hypothesis hypothesis, final AnalysisParameters parameters) throws InterruptedException {
        final ArrayList<Future<LocusProbability>> futures = new ArrayList<>();
        final ArrayList<LocusProbabilityJob> jobs = new ArrayList<>();
        try {

            // Only evaluate loci that are present in the crime stain and the current candidate
            final List<String> enabledLoci = MessageBus.getInstance().query(EnabledLociMessage.class);
            final ArrayList<String> commonLoci = new ArrayList<>();
            final Sample candidate = hypothesis.getCandidate();
            if (candidate != null) {
                for (final String locusName : enabledLoci) {
                    if (candidate.hasLocus(locusName)) {
                        commonLoci.add(locusName);
                    }
                }
            }
            else {
                commonLoci.addAll(enabledLoci);
            }

            for (final String locusName : commonLoci) {
                boolean generateJobs = true;
                if (candidate != null) {
                    final Locus candidateLocus = candidate.getLocus(locusName);
                    final double[][] locusCache = _resultCache[candidateLocus.getId()];
                    if (candidateLocus.size() != 2) {
                        throw new IllegalArgumentException("Cannot process locus " + locusName + " of sample " + candidate.getName() + ". Expected 2 alleles but found " + candidateLocus.size() + ": " + candidateLocus.getAlleles());
                    }
                    final Allele[] alleles = candidateLocus.getAlleles().toArray(new Allele[0]);
                    final Allele firstAllele = alleles[0];
                    final Allele secondAllele = alleles[1];
                    if (locusCache[firstAllele.getId()][secondAllele.getId()] != 0) {
                        futures.add(new FixedResultJob(hypothesis, locusName, locusCache[firstAllele.getId()][secondAllele.getId()]));
                        generateJobs = false;
                    }
                }

                if (generateJobs) {
                    final Locus[] possibleAlleleCombinations = composePossibleAlleleCombinations(locusName, parameters, hypothesis);
                    for (final LocusProbabilityJob job : LocusProbabilityJobGenerator.generate(locusName, parameters, possibleAlleleCombinations, hypothesis)) {
                        futures.add(SERVICE.submit(job));
                        jobs.add(job);
                    }
                }
            }

            _resultsCollector = new ResultsCollector(futures, !hypothesis.hasCandidate() && parameters.isCalculateHdOnce());
            _resultsCollector.start();
            _resultsCollector.join();
            final LocusLikelihoods result = _resultsCollector.getLikelihoods();

            if (candidate != null) {
                for (final String locusName : result.getLoci()) {
                    final Locus candidateLocus = candidate.getLocus(locusName);
                    final Allele[] alleles = candidateLocus.getAlleles().toArray(new Allele[0]);
                    final Allele firstAllele = alleles[0];
                    final Allele secondAllele = alleles[1];
                    final double[][] locusCache = _resultCache[candidateLocus.getId()];
                    locusCache[firstAllele.getId()][secondAllele.getId()] = result.getLocusProbability(locusName);
                }
            }
            return result;
        }
        catch (final InterruptedException ie) {
            throw ie;
        }
        catch (final Throwable t) {
            LOG.error("Error running search!", t);
            throw t;
        }
        finally {
            for (final LocusProbabilityJob job : jobs) {
                job.interrupt();
            }
        }
    }

    @Override
    public String getModelName() {
        return "SmartRankModel";
    }

    @Override
    public void interrupt() {
        if (_resultsCollector != null) {
            _resultsCollector.interrupt();
        }
    }

    private Locus[] composePossibleAlleleCombinations(final String locusName, final AnalysisParameters parameters, final Hypothesis hypothesis) {
        Locus[] alleleCombinations = _possibleAlleleCombinationsPerLocus[Locus.getId(locusName)];
        if (alleleCombinations == null) {

            // Create the set of all possible allele combinations at the current locus
            final HashMap<String, Locus> possibleAlleleCombinations = new HashMap<>();
            final Collection<String> alleleCollection = new ArrayList<>();

            // Add alleles present in the crimescene profiles
            for (final Sample replicate : parameters.getEnabledCrimesceneProfiles()) {
                final Locus replicateLocus = replicate.getLocus(locusName);
                if (replicateLocus != null) {
                    for (final Allele replicateAllele : replicateLocus.getAlleles()) {
                        if (!alleleCollection.contains(replicateAllele.getAllele())) {
                            alleleCollection.add(replicateAllele.getAllele());
                        }
                    }
                }
            }

            // If Q is not shut down, add other alleles
            if (!hypothesis.isQDesignationShutdown()) {
                // Add alleles in the contributor samples (only non-rare alleles)
                for (final Contributor con : hypothesis.getContributors()) {
                    Locus conLocus = con.getSample().getLocus(locusName);
                    if (conLocus == null) {
                        conLocus = new Locus(locusName);
                        conLocus.setSample(con.getSample());
                    }
                    for (final Allele conAllele : conLocus.getAlleles()) {
                        if (!alleleCollection.contains(conAllele.getAllele()) && !hypothesis.getPopulationStatistics().isRareAllele(conAllele)) {
                            alleleCollection.add(conAllele.getAllele());
                        }
                    }
                }

                // Add alleles in the non-contributor samples (only non-rare alleles)
                for (final Contributor nonCon : hypothesis.getNonContributors()) {
                    Locus nonConLocus = nonCon.getSample().getLocus(locusName);
                    if (nonConLocus == null) {
                        nonConLocus = new Locus(locusName);
                        nonConLocus.setSample(nonCon.getSample());
                    }
                    for (final Allele nonConAllele : nonConLocus.getAlleles()) {
                        if (!alleleCollection.contains(nonConAllele.getAllele()) && !hypothesis.getPopulationStatistics().isRareAllele(nonConAllele)) {
                            alleleCollection.add(nonConAllele.getAllele());
                        }
                    }
                }

                // Add a single allele that has the combined probabilities of all alleles not in the samples and profiles
                Double otherFrequency = 0.0;
                for (final String allele : hypothesis.getPopulationStatistics().getAlleles(locusName)) {
                    if (!allele.endsWith("-other") && !alleleCollection.contains(allele)) {
                        otherFrequency += hypothesis.getPopulationStatistics().getProbability(locusName, allele);
                    }
                }

                // Do not add the combined allele if its frequency is not a number, infinity or zero
                if (!otherFrequency.isInfinite() && !otherFrequency.isNaN() && otherFrequency > 0) {
                    alleleCollection.add(locusName + "-other");
                    hypothesis.getPopulationStatistics().addStatistic(locusName, locusName + "-other", new BigDecimal(otherFrequency));
                }
            }

            // Convert the collection of allele values to a list of Locus classes
            final String[] alleles = alleleCollection.toArray(new String[0]);
            Arrays.sort(alleles);
            for (int allele1Idx = 0; allele1Idx < alleles.length; allele1Idx++) {
                for (int allele2Idx = allele1Idx; allele2Idx < alleles.length; allele2Idx++) {
                    final String name = allele1Idx + "." + allele2Idx;
                    if (!possibleAlleleCombinations.containsKey(name)) {
                        final Locus newLocus = new Locus(locusName);
                        newLocus.addAllele(new Allele(alleles[allele1Idx]));
                        newLocus.addAllele(new Allele(alleles[allele2Idx]));
                        possibleAlleleCombinations.put(name, newLocus);
                    }
                }
            }

            alleleCombinations = possibleAlleleCombinations.values().toArray(new Locus[possibleAlleleCombinations.size()]);

            LOG.debug("Possible Allele Combinations: {}", (Object) alleleCombinations);
            _possibleAlleleCombinationsPerLocus[Locus.getId(locusName)] = alleleCombinations;
        }

        return alleleCombinations;
    }

    @Override
    public void reset() {
        for (int idx = 0; idx < _possibleAlleleCombinationsPerLocus.length; idx++) {
            _possibleAlleleCombinationsPerLocus[idx] = null;
        }
    }
}
