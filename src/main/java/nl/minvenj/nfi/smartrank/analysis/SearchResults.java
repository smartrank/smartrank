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

package nl.minvenj.nfi.smartrank.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.LikelihoodRatio;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.domain.Sample;

/**
 * Contains the results of the last search action.
 */
public class SearchResults {
    private final double[] _lrs;
    private int _totalResultCount;
    private Double _maxRatio;
    private Double _minRatio;
    private long _duration;
    private int _resultsOver1;
    private String _logfileName;
    private boolean _succeeded;
    private Throwable _failureReason;
    private final List<LikelihoodRatio> _positiveLRs;
    private DefenseHypothesis _hd;
    private ProsecutionHypothesis _hp;
    private final ArrayList<ExcludedProfile> _excludedProfiles;
    private String _reportFileName;
    private final long _startTime;
    private AnalysisParameters _parameters;
    private final HashMap<ExclusionReason, ExcludedProfileStatistic> _exclusionStats;
    private final Map<String, Map<String, Integer>> _metadataStatistics;
    private final DatabaseConfiguration _config;

    /**
     * Constructor. Creates a {@link SearchResults} object to hold the specified number of results.
     *
     * @param databaseSize The number of records in the database. Used to initialize the internal structure of this object.
     */
    public SearchResults(final int databaseSize, final DatabaseConfiguration config) {
        _excludedProfiles = new ArrayList<>();
        _positiveLRs = new ArrayList<>();
        _minRatio = Double.MAX_VALUE;
        _maxRatio = Double.MIN_VALUE;
        _totalResultCount = 0;
        _startTime = System.currentTimeMillis();
        _exclusionStats = new HashMap<>();
        _metadataStatistics = new HashMap<>();
        _config = config;
        _lrs = new double[databaseSize];
    }

    /**
     * Composes an identifying string for the combination of supplied {@link Hypothesis} objects.
     *
     * @param hp the prosecution {@link Hypothesis}
     * @param hd the defense {@link Hypothesis}
     * @return a String containing a description of the combination of supplied {@link Hypothesis} objects
     */
    public static String getGuid(final Hypothesis hp, final Hypothesis hd) {
        return hp.getGuid() + "|" + hd.getGuid();
    }

    /**
     * Gets the number of LR results stored in this {@link SearchResults} object.
     *
     * @return The number of search result LRs stored
     */
    public int getNumberOfLRs() {
        return _totalResultCount;
    }

    /**
     * Gets the number of stored search results that exceed 1.0.
     *
     * @return The number of LRs that have a value over 1.0 (i.e. where the evaluation results in a value in favour of the prosecution)
     */
    public int getNumberOfLRsOver1() {
        return _resultsOver1;
    }

    /**
     * Adds a {@link LikelihoodRatio} to the search results.
     *
     * @param lr the {@link LikelihoodRatio} to add to the results
     */
    synchronized void addLR(final LikelihoodRatio lr) {
        final Double ratio = lr.getOverallRatio().getRatio();
        _lrs[_totalResultCount++] = ratio;

        if (!ratio.isNaN() && !ratio.isInfinite()) {
            _maxRatio = Math.max(ratio, _maxRatio);
            _minRatio = Math.min(ratio, _minRatio);

            if (ratio > 1) {
                _positiveLRs.add(lr);
                Collections.sort(_positiveLRs, new Comparator<LikelihoodRatio>() {
                    @Override
                    public int compare(final LikelihoodRatio o1, final LikelihoodRatio o2) {
                        return -o1.compareTo(o2);
                    }
                });
                _resultsOver1++;
            }
        }
    }

    /**
     * Gets the collection of positive LR results, in descending order order.
     *
     * @return a {@link Collection} containing the LRs greater than 1 resulting from the search in descending order.
     */
    public List<LikelihoodRatio> getPositiveLRs() {
        return _positiveLRs;
    }

    /**
     * Gets the {@link LikelihoodRatio}s stored in this object.
     *
     * @return a {@link Collection} of {@link LikelihoodRatio} objects reflecting the search results
     */
    public List<Double> getLRs() {
        final ArrayList<Double> lrs = new ArrayList<>();
        for (int idx = 0; idx < _totalResultCount; idx++) {
            lrs.add(_lrs[idx]);
        }
        return Collections.unmodifiableList(lrs);
    }

    /**
     * Gets the maximum LR stored in this object.
     *
     * @return a Double holding the highest LR in the results object
     */
    public Double getMaxRatio() {
        return _maxRatio;
    }

    /**
     * Gets the minimum LR stored in this object.
     *
     * @return a Double holding the lowest LR in the results object
     */
    public Double getMinRatio() {
        return _minRatio;
    }

    /**
     * Gets the running time of the search.
     *
     * @return a long value holding the number of milliseconds that the search took
     */
    public long getDuration() {
        return _duration;
    }

    /**
     * sets the duration of the search that generated this {@link SearchResults} object.
     *
     * @param duration the duration of the search, specified in milliseconds
     */
    public void setDuration(final long duration) {
        _duration = duration;
    }

    /**
     * Gets the search results grouped by number of evaluated loci.
     *
     * @return a {@link Collection} of {@link ResultsPerLocus} objects holding the number of results per evaluated number of loci
     */
    public Collection<ResultsPerLocus> getResultsPerNumberOfLoci(final int threshold) {
        final ArrayList<ResultsPerLocus> results = new ArrayList<>();
        final int[] resultsPerNumberOfLoci = new int[Locus.getRegisteredLocusCount() + 1];
        for (final LikelihoodRatio lr : _positiveLRs) {
            if (lr.getOverallRatio().getRatio() > threshold) {
                resultsPerNumberOfLoci[lr.getProfile().size()]++;
            }
        }

        for (int idx = 0; idx < resultsPerNumberOfLoci.length; idx++) {
            if (resultsPerNumberOfLoci[idx] > 0) {
                results.add(new ResultsPerLocus(idx, resultsPerNumberOfLoci[idx]));
            }
        }
        return results;
    }

    /**
     * Gets a percentile value of the distribution of the stored results.
     *
     * @param percentile the requested percentile value
     *
     * @return The computed percentile value of the results
     */
    public double getPercentile(final int percentile) {
        final Percentile p = new Percentile(percentile);
        final double[] sorted = new double[_totalResultCount];
        for (int idx = 0; idx < _totalResultCount; idx++) {
            sorted[idx] = _lrs[idx];
        }
        Arrays.sort(sorted);
        p.setData(sorted);
        return p.evaluate();
    }

    /**
     * Gets the name of the logfile generated by this search.
     *
     * @return A String containing the logfile name
     */
    public String getLogFileName() {
        return _logfileName;
    }

    /**
     * Sets the name of the logfile generated by the search.
     *
     * @param name a String containing the name of the logfile
     */
    public void setLogFileName(final String name) {
        _logfileName = name;
    }

    /**
     * Sets the Succeeded flag of the search results.
     */
    public void setSucceeded() {
        _succeeded = true;
        _failureReason = null;
    }

    /**
     * Gets the Succeeded flag of the search results. A search succeeds if all eligible records were evaluated without encountering an exception or being interrupted.
     * Note that success of a search does not relate to finding any matches! A search that finds no matches can still succeed.
     *
     * @return <code>true</code> if the search succeeded (i.e. no exceptions were encountered), <code>false</code> otherwise.
     */
    public boolean isSucceeded() {
        return _succeeded;
    }

    /**
     * Signals that the search failed due to the Throwable supplied.
     *
     * @param reason a {@link Throwable} that caused the search to be abandoned
     */
    public void setFailed(final Throwable reason) {
        _failureReason = reason;
    }

    /**
     * Gets the reason why a search was abandoned.
     *
     * @return a {@link Throwable} that caused the search to be abandoned. Can be null if the search was successful.
     */
    public Throwable getFailureReason() {
        return _failureReason;
    }

    /**
     * Generates a value identifying the input data that lead to this particular result.
     *
     * @return an {@link Integer} identifying this result
     */
    public String getGuid() {
        return getGuid(_hp, _hd);
    }

    /**
     * Gets the {@link DefenseHypothesis} that was used to arrive at these results.
     *
     * @return a {@link DefenseHypothesis} representing the defense settings
     */
    public DefenseHypothesis getDefenseHypothesis() {
        return _hd;
    }

    /**
     * Gets the {@link ProsecutionHypothesis} that was used to arrive at these results.
     *
     * @return a {@link ProsecutionHypothesis} representing the prosecution settings
     */
    public ProsecutionHypothesis getProsecutionHypothesis() {
        return _hp;
    }

    /**
     * sets the {@link ProsecutionHypothesis} that will be used to perform a search.
     *
     * @param hp a {@link ProsecutionHypothesis} containing the prosecution settings
     */
    public void setProsecution(final ProsecutionHypothesis hp) {
        _hp = (ProsecutionHypothesis) hp.copy();
    }

    /**
     * Sets the {@link DefenseHypothesis} that will be used to perform a search.
     *
     * @param hd a {@link DefenseHypothesis} containing the defense settings
     */
    public void setDefense(final DefenseHypothesis hd) {
        _hd = (DefenseHypothesis) hd.copy();
    }

    /**
     * Indicates that a sample was ignored in the search, with a corresponding reason.
     *
     * @param profile the {@link Sample} that was ignored
     * @param reason the {@link ExclusionReason} why this profile is ignored
     */
    public void addExcludedProfile(final Sample profile, final ExclusionReason reason) {
        final ExcludedProfile ignoredProfile = new ExcludedProfile(profile, reason);
        if (!_excludedProfiles.contains(ignoredProfile)) {
            _excludedProfiles.add(ignoredProfile);
            ExcludedProfileStatistic excludedProfileStatistic = _exclusionStats.get(reason);
            if (excludedProfileStatistic == null) {
                excludedProfileStatistic = new ExcludedProfileStatistic(reason);
                _exclusionStats.put(reason, excludedProfileStatistic);
            }
            excludedProfileStatistic.add();
        }
    }

    /**
     * Gets a collection of excluded profiles and corresponding reasons.
     *
     * @return a {@link Collection} of {@link ExcludedProfile}s
     */
    public List<ExcludedProfile> getExcludedProfiles() {
        return _excludedProfiles;
    }

    /**
     * Gets the number of excluded profiles grouped by reason.
     *
     * @return a {@link Collection} of {@link ExcludedProfileStatistic}s
     */
    public Collection<ExcludedProfileStatistic> getExcludedProfileStatistics() {
        return _exclusionStats.values();
    }

    /**
     * Gets a list of profiles that were excluded from the search for the given reason.
     *
     * @param reason the {@link ExclusionReason} for which to return sample names
     * @return a {@link Collection} of profile names for excluded profiles. If no profiles were excluded for the supplied reason, an empty collection is returned.
     */
    public List<String> getExcludedProfileNames(final ExclusionReason reason) {
        final ArrayList<String> excludedProfileNames = new ArrayList<>();
        for (final ExcludedProfile excludedProfile : _excludedProfiles) {
            if (excludedProfile.getReason() == reason) {
                excludedProfileNames.add(excludedProfile.getSampleName());
            }
        }
        return excludedProfileNames;
    }

    /**
     * Adds the supplied profiles to the list of excluded profiles.
     *
     * @param badRecordList a {@link List} of {@link ExcludedProfile}s
     */
    public void addExcludedProfiles(final List<ExcludedProfile> badRecordList) {
        _excludedProfiles.addAll(badRecordList);
        for (final ExcludedProfile excludedProfile : badRecordList) {
            ExcludedProfileStatistic excludedProfileStatistic = _exclusionStats.get(excludedProfile.getReason());
            if (excludedProfileStatistic == null) {
                excludedProfileStatistic = new ExcludedProfileStatistic(excludedProfile.getReason());
                _exclusionStats.put(excludedProfile.getReason(), excludedProfileStatistic);
            }
            excludedProfileStatistic.add();
        }
    }

    /**
     * Sets the name of the report generated by the search (if any).
     *
     * @param name a String containing the name of the report file, or null if no report was generated.
     */
    public void setReportName(final String reportFileName) {
        _reportFileName = reportFileName;
    }

    /**
     * Gets the name of the report generated by this search (if any).
     *
     * @return A String containing the report filename or null if no report was generated.
     */
    public String getReportFileName() {
        return _reportFileName;
    }

    public long getStartTime() {
        return _startTime;
    }

    public void setParameters(final AnalysisParameters parameters) {
        _parameters = new AnalysisParameters(parameters);
    }

    public AnalysisParameters getParameters() {
        return _parameters;
    }

    public boolean isInterrupted() {
        Throwable reason = getFailureReason();
        Throwable cause = reason;
        if (reason != null)
            cause = reason.getCause();
        while (reason != cause) {
            if (reason instanceof InterruptedException)
                return true;
            reason = cause;
            if (reason != null)
                cause = reason.getCause();
        }
        return false;
    }

    public void setProfileMetadataStatistics(final Map<String, Map<String, Integer>> stats) {
        _metadataStatistics.clear();
        _metadataStatistics.putAll(stats);
    }

    public Map<String, Map<String, Integer>> getProfileMetadataStatistics() {
        return _metadataStatistics;
    }

    public DatabaseConfiguration getDatabaseConfiguration() {
        return _config;
    }
}
