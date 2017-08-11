/*
 * Copyright (C) 2015,2016 Netherlands Forensic Institute
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

import static nl.minvenj.nfi.smartrank.raven.timeformat.TimeUtils.formatDuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import nl.minvenj.nfi.smartrank.SmartRank;
import nl.minvenj.nfi.smartrank.analysis.parameterestimation.DropoutEstimation;
import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.DNADatabase;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.LikelihoodRatio;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.domain.Ratio;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.io.WritableFileSource;
import nl.minvenj.nfi.smartrank.messages.commands.WritableFileSourceMessage;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.messages.data.CrimeSceneProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseMessage;
import nl.minvenj.nfi.smartrank.messages.data.EnabledLociMessage;
import nl.minvenj.nfi.smartrank.messages.data.KnownProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.PopulationStatisticsMessage;
import nl.minvenj.nfi.smartrank.raven.NullUtils;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.raven.numberformat.NumberUtils;
import nl.minvenj.nfi.smartrank.utils.DomainExpressionResolver;
import nl.minvenj.nfi.smartrank.utils.OutputLocationResolver;

/**
 * Handles logging of case-related data and results.
 */
class CaseLogger {

    private static final int MAX_REPORTED_EXCLUDED_PROFILE_NAMES = 50;
    private static final Logger LOG = LoggerFactory.getLogger(CaseLogger.class);
    private static final int FIELD_WIDTH = 30;
    private static final int NUMBER_OF_DECIMALS = 6;

    private final Logger _caseLogger;
    private final MessageBus _messageBus;
    private long _start;
    private long _duration;
    private String _logFileName;
    private final long _startTime;

    public CaseLogger() {
        _messageBus = MessageBus.getInstance();
        _startTime = System.currentTimeMillis();
        _caseLogger = initLogger();
    }

    private Logger initLogger() {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ch.qos.logback.classic.Logger caseLogger = context.getLogger("CaseLogger");

        FileAppender<ILoggingEvent> caseAppender = (FileAppender<ILoggingEvent>) caseLogger.getAppender("CaseLog");
        if (caseAppender != null) {
            caseLogger.detachAppender(caseAppender);
            caseAppender.stop();
        }

        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%m%n");
        encoder.start();

        caseAppender = new FileAppender<>();
        caseAppender.setContext(context);
        caseAppender.setName("CaseLog");
        caseAppender.setEncoder(encoder);

        caseLogger.addAppender(caseAppender);

        _logFileName = OutputLocationResolver.resolve(DomainExpressionResolver.resolve(SmartRankRestrictions.getCaseLogFilename(), _startTime));

        final WritableFileSource writableFileSource = _messageBus.query(WritableFileSourceMessage.class);
        if (writableFileSource != null) {
            final String writableFileName = writableFileSource.getWritableFile(_logFileName);
            if (!writableFileName.isEmpty()) {
                _logFileName = writableFileName;
            }
            else {
                LOG.error("Could not select a writable file!");
                throw new IllegalArgumentException(_logFileName + " is not writable!");
            }
        }

        caseAppender.setFile(_logFileName);
        caseAppender.start();

        if (!caseAppender.isStarted()) {
            LOG.error("Could not start case logger: {}", context.getStatusManager().getCopyOfStatusList());
        }

        return caseLogger;
    }

    private void resetLogger() {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ch.qos.logback.classic.Logger caseLogger = context.getLogger("CaseLogger");
        final Appender<ILoggingEvent> appender = caseLogger.getAppender("CaseLog");
        if (appender != null) {
            caseLogger.getAppender("CaseLog").stop();
            caseLogger.detachAppender("CaseLog");
        }
    }

    /**
     * Logs the contents of a sample.
     *
     * @param candidateSample the sample to log
     * @param lr the likelihood ratio for the sample
     */
    public synchronized void logResult(final Sample candidateSample, final LikelihoodRatio lr) {
        double prEP = 1;
        double prED = 1;
        _caseLogger.info("=========== {} Log10(LR) vs. Pr(D) ===========", candidateSample);
        _caseLogger.info("  Locus      Pr(E|Hp)                       Pr(E|Hd)                       LR                             log10(LR)");
        _caseLogger.info("  ---------------------------------------------------------------------------------------------------------------------------------");
        for (final String locus : _messageBus.query(EnabledLociMessage.class)) {
            for (final Ratio ratio : lr.getRatios()) {
                if (ratio.getLocusName().equalsIgnoreCase(locus)) {
                    _caseLogger.info("  {} {} {} {} {}",
                                     addPadding(locus, 10),
                                     addPadding(NumberUtils.format(NUMBER_OF_DECIMALS, ratio.getProsecutionProbability()), FIELD_WIDTH),
                                     addPadding(NumberUtils.format(NUMBER_OF_DECIMALS, ratio.getDefenseProbability()), FIELD_WIDTH),
                                     addPadding(NumberUtils.format(NUMBER_OF_DECIMALS, ratio.getRatio()), FIELD_WIDTH),
                                     NumberUtils.format(NUMBER_OF_DECIMALS, Math.log10(ratio.getRatio())));
                    prEP *= ratio.getProsecutionProbability();
                    prED *= ratio.getDefenseProbability();
                }
            }
        }
        _caseLogger.info("  ---------------------------------------------------------------------------------------------------------------------------------");
        _caseLogger.info("  {} {} {} {} {}", addPadding("Product", 10), addPadding(NumberUtils.format(NUMBER_OF_DECIMALS, prEP), FIELD_WIDTH), addPadding(NumberUtils.format(NUMBER_OF_DECIMALS, prED), FIELD_WIDTH), addPadding(NumberUtils.format(NUMBER_OF_DECIMALS, lr.getOverallRatio().getRatio()), FIELD_WIDTH), NumberUtils.format(NUMBER_OF_DECIMALS, Math.log10(lr.getOverallRatio().getRatio())));
    }

    private String addPadding(final String value, final int length) {
        final StringBuilder retval = new StringBuilder(value);
        while (retval.length() < length) {
            retval.append(" ");
        }
        return retval.toString();
    }

    private String addLeftPadding(final String value, final int length) {
        final StringBuilder retval = new StringBuilder(value);
        while (retval.length() < length) {
            retval.insert(0, " ");
        }
        return retval.toString();
    }

    /**
     * Logs a footer for normal completion of a search, reporting the number of specimens, number of matches and the time taken.
     *
     * @param specimenCount the number of specimens evaluated
     */
    public synchronized void logFooter(final SearchResults searchResults, final int specimenCount) {
        _duration = System.currentTimeMillis() - _start;
        _caseLogger.info("=================");
        _caseLogger.info("  Analysis Completed");
        _caseLogger.info("  Number of specimens: {}", specimenCount);
        _caseLogger.info("");
        int count = 0;
        for (final LikelihoodRatio lr : searchResults.getPositiveLRs()) {
            if (lr.getOverallRatio().getRatio() > searchResults.getParameters().getLrThreshold()) {
                count++;
            }
        }
        final Map<String, Map<String, Integer>> metadataStatistics = searchResults.getProfileMetadataStatistics();
        for (final String statType : metadataStatistics.keySet()) {
            final Map<String, Integer> statValues = metadataStatistics.get(statType);
            _caseLogger.info("    Overview of specimens by {}", statType);
            _caseLogger.info("    ------------------------------------------------");
            for (final String statName : statValues.keySet()) {
                _caseLogger.info("      {} : {}", addPadding(statName, 25), statValues.get(statName));
            }
            _caseLogger.info("");
        }
        _caseLogger.info("  Number of LRs over {}: {}", searchResults.getParameters().getLrThreshold(), count);
        _caseLogger.info("  Running time: {}", formatDuration(_duration));
        resetLogger();
    }

    /**
     * Logs a footer for an interrupted search, reporting number of specimens evaluated, percent of total specimens evaluated, number of matches and time taken.
     *
     * @param ie the InterruptedException that caused this footer to be logged
     * @param percentReady the percentage of total specimens that were evaluated
     * @param specimenIndex the number of specimens evaluated
     */
    public synchronized void logFooter(final SearchResults searchResults, final int percentReady, final int specimenIndex) {
        final long runningTime = System.currentTimeMillis() - _start;
        _caseLogger.info("=================");
        if (searchResults.getFailureReason() instanceof InterruptedException)
            _caseLogger.info("  Analysis Interrupted after {} specimens ({}%) ", specimenIndex, percentReady);
        else
            _caseLogger.info("  Analysis encountered an error: {}", searchResults.getFailureReason().getMessage(), searchResults.getFailureReason());

        int count = 0;
        for (final LikelihoodRatio lr : searchResults.getPositiveLRs()) {
            if (lr.getOverallRatio().getRatio() > searchResults.getParameters().getLrThreshold()) {
                count++;
            }
        }
        _caseLogger.info("  Number of LRs over {}: {}", searchResults.getParameters().getLrThreshold(), count);
        _caseLogger.info("  Running time: {}", formatDuration(runningTime));
        resetLogger();
    }

    /**
     * Logs a header listing the search parameters.
     *
     * @param modelName The name of the model used for the search
     */
    public synchronized void logHeader(final String modelName) {
        logHeader(modelName, null);
    }

    /**
     * Logs a header listing the search parameters.
     *
     * @param modelName the name of the model used for the search
     * @param automaticDropoutEstimation a {@link DropoutEstimation} object containing the results of a dropout estimation. Can be null.
     */
    public synchronized void logHeader(final String modelName, final DropoutEstimation automaticDropoutEstimation) {
        final AnalysisParameters parameters = _messageBus.query(AnalysisParametersMessage.class);
        final PopulationStatistics popStats = _messageBus.query(PopulationStatisticsMessage.class);
        final DNADatabase dnaDatabase = _messageBus.query(DatabaseMessage.class);

        _start = System.currentTimeMillis();
        _caseLogger.info(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        _caseLogger.info("  SmartRank version {}", SmartRank.getRevision());
        final String[] signatureStrings = SmartRank.getSignatureInfo().split("(</LI>|<UL>)");
        for (final String sig : signatureStrings) {
            final String cleaned = sig.replaceAll("<LI>", "  ").replaceAll("<[^>]+>", "");
            if (!cleaned.isEmpty()) {
                _caseLogger.info("  {}", cleaned);
            }
        }

        _caseLogger.info("  Analysis started by {} on {}", System.getProperty("user.name"), getHostName());
        _caseLogger.info("  Statistical Model: {}", modelName);
        _caseLogger.info("  LR Threshold: {}", parameters.getLrThreshold());
        _caseLogger.info("  Max memory: {} bytes, {} MB", Runtime.getRuntime().maxMemory(), Runtime.getRuntime().maxMemory() / 1048576);
        _caseLogger.info("  Java version: {}", System.getProperty("java.version"));
        _caseLogger.info("  Java home: {}", System.getProperty("java.home"));
        _caseLogger.info("  Pr(E|Hd) calculation {} optimized", parameters.isCalculateHdOnce() ? "is" : "is not");
        _caseLogger.info("=================");
        _caseLogger.info("  Database: {}", dnaDatabase.getConnectString());
        _caseLogger.info("    Format: {}", dnaDatabase.getFormatName());
        _caseLogger.info("    Hash: {}", dnaDatabase.getFileHash());
        _caseLogger.info("    Size: {}", dnaDatabase.getRecordCount());
        _caseLogger.info("    Composition:");
        _caseLogger.info("");
        _caseLogger.info("    # of Loci |   # of Specimens  | Percent of total ");
        _caseLogger.info("    ----------+-------------------+------------------");

        final List<Integer> specimenCountPerNumberOfLoci = dnaDatabase.getSpecimenCountPerNumberOfLoci();
        for (int locusCount = 0; locusCount < specimenCountPerNumberOfLoci.size(); locusCount++) {
            final Integer specimenCount = specimenCountPerNumberOfLoci.get(locusCount);
            if (specimenCount > 0) {
                final String percent = String.format("%2.2f", ((((long) specimenCount * 10000L) / dnaDatabase.getRecordCount()) / 100F));
                _caseLogger.info("      {}   |   {}   |    {}",
                                 addLeftPadding("" + locusCount, 5),
                                 addLeftPadding("" + specimenCount, 13),
                                 ("" + addLeftPadding(percent, 6)));
            }

        }

        _caseLogger.info("");
        logQuery("Specimen IDs", dnaDatabase.getConfiguration().getSpecimenKeyQuery());
        logQuery("Specimen Data", dnaDatabase.getConfiguration().getSpecimenQuery());
        logQuery("Database Revision", dnaDatabase.getConfiguration().getDatabaseRevisionQuery());

        _caseLogger.info("=================");
        _caseLogger.info("  Statistics file: {}", popStats == null ? "No statistics file Loaded!" : popStats.getFileName());
        _caseLogger.info("  Statistics file hash: {}", popStats == null ? "No statistics file Loaded!" : popStats.getFileHash());
        _caseLogger.info("  Rare Allele Frequency: {}", popStats == null ? "No statistics file Loaded!" : popStats.getRareAlleleFrequency());
        _caseLogger.info("=================");
        _caseLogger.info("Loaded replicates:");
        for (final Sample sample : parameters.getEnabledCrimesceneProfiles()) {
            _caseLogger.info("  {} loaded from '{}' file hash {}", sample.getName(), sample.getSourceFile(), sample.getSourceFileHash());
        }
        _caseLogger.info("=================");
        _caseLogger.info("Loaded profiles:");
        final List<Sample> knownProfiles = _messageBus.query(KnownProfilesMessage.class);
        if (knownProfiles == null || knownProfiles.isEmpty()) {
            _caseLogger.info("  No known profiles loaded.");
        }
        else {
            for (final Sample sample : knownProfiles) {
                _caseLogger.info("  {} loaded from '{}' file hash {}", sample.getName(), sample.getSourceFile(), sample.getSourceFileHash());
            }
        }
        _caseLogger.info("=================");
        _caseLogger.info("Enabled loci: {}", _messageBus.query(EnabledLociMessage.class));

        _caseLogger.info("=================");
        final Collection<Allele> rareAlleles = getRareAlleles(popStats);
        if (!rareAlleles.isEmpty()) {
            _caseLogger.info("The following alleles were detected as rare:");
            for (final Allele a : rareAlleles) {
                _caseLogger.info("  {} at locus {} of {}", a.getAllele(), a.getLocus().getName(), a.getLocus().getSample().getName());
            }
            _caseLogger.info("These alleles have been assigned the following frequency: {}", popStats == null ? "No statistics file Loaded!" : popStats.getRareAlleleFrequency());
        }
        else {
            _caseLogger.info("No rare alleles detected");
        }

        _caseLogger.info("=================");
        if (automaticDropoutEstimation != null) {
            _caseLogger.info("Parameter estimation was performed automatically.");
            _caseLogger.info("  Iterations:           {}", automaticDropoutEstimation.getIterations());
            _caseLogger.info("  Dropout Distribution: {}", automaticDropoutEstimation);
            _caseLogger.info("  SmartRank is configured to use the {}% percentile of the dropout distribution", automaticDropoutEstimation.getDropoutEstimationPercentile());
            _caseLogger.info("  The dropout value at this percentile is {}", automaticDropoutEstimation.getEstimatedDropout());
        }
        else {
            final DropoutEstimation manualEstimate = parameters.getDropoutEstimation();
            if (manualEstimate != null) {
                _caseLogger.info("Parameter estimation was performed manually.");
                _caseLogger.info("  Iterations: {}", manualEstimate.getIterations());
                _caseLogger.info("  Dropout Distribution: {}", manualEstimate);
                _caseLogger.info("  SmartRank is configured to use the % percentile of the dropout distribution", manualEstimate.getDropoutEstimationPercentile());
                _caseLogger.info("  The dropout value at this percentile is {}", manualEstimate.getEstimatedDropout());
            }
            else {
                _caseLogger.info("Parameter estimation was not performed.");
            }
        }
    }

    private void logQuery(final String name, final String specimenKeyQuery) {
        final String query = NullUtils.getValue(specimenKeyQuery, "").trim();
        if (!query.isEmpty()) {
            _caseLogger.info("{} obtained using query:", name);
            _caseLogger.info("    {}", query.replaceAll("\n", "\n    "));
            _caseLogger.info("");
        }
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (final UnknownHostException ex) {
            return ex.getClass().getName() + " - " + ex.getMessage();
        }
    }

    /**
     * Logs the contents of a hypothesis.
     *
     * @param hypothesis the {@link Hypothesis} to log
     */
    public synchronized void logHypothesis(final Hypothesis hypothesis) {
        _caseLogger.info("=================");
        _caseLogger.info("Hypothesis {}", hypothesis.getId());
        _caseLogger.info("  Contributors {}", hypothesis.getContributors());
        _caseLogger.info("  Non-Contributors {}", hypothesis.getNonContributors());
        _caseLogger.info("  Unknowns {}", hypothesis.getUnknownCount());
        _caseLogger.info("  Unknown Dropout {}", new BigDecimal(hypothesis.getUnknownDropoutProbability()).setScale(2, RoundingMode.HALF_UP));
        _caseLogger.info("  Dropin {}", new BigDecimal(hypothesis.getDropInProbability()).setScale(2, RoundingMode.HALF_UP));
        _caseLogger.info("  Theta {}", new BigDecimal(hypothesis.getThetaCorrection()).setScale(2, RoundingMode.HALF_UP));
        _caseLogger.info("  Q-Designation {} shut down", hypothesis.isQDesignationShutdown() ? "is" : "is not");
    }

    /**
     * Logs the final ranking of matches.
     */
    public synchronized void logRanking(final SearchResults searchResults) {
        _caseLogger.info("=================");
        _caseLogger.info("Final ranking:");
        _caseLogger.info("  +--------------------------------+----------+---------------+----------+------------------------------------------------------------------+");
        _caseLogger.info("  | ID                             | Rank     | log10(LR)     | #Loci    | Comments                                                         |");
        _caseLogger.info("  +--------------------------------+----------+---------------+----------+------------------------------------------------------------------+");

        int rank = 0;
        for (final LikelihoodRatio ratio : searchResults.getPositiveLRs()) {
            if (ratio.getOverallRatio().getRatio() > searchResults.getParameters().getLrThreshold()) {
                rank++;
                _caseLogger.info("  | {} | {} | {} | {} | {} |",
                                 addPadding(ratio.getProfile().getName(), 30),
                                 addPadding("" + rank, 8),
                                 addPadding(NumberUtils.format(3, Math.log10(ratio.getOverallRatio().getRatio())), 13),
                                 addPadding("" + ratio.getProfile().getLoci().size(), 8),
                                 addPadding(ratio.getProfile().getAdditionalData(), 64));
            }
        }
        _caseLogger.info("  +--------------------------------+----------+---------------+----------+------------------------------------------------------------------+");
    }

    private Collection<Allele> getRareAlleles(final PopulationStatistics popstats) {
        final Collection<Sample> crimesceneProfiles = _messageBus.query(CrimeSceneProfilesMessage.class);
        final Collection<Allele> rareAlleles = new ArrayList<>();
        if (popstats != null) {
            for (final Sample sample : crimesceneProfiles) {
                addRareAlleles(sample, popstats, rareAlleles);
            }

            final Collection<Sample> knownProfiles = _messageBus.query(KnownProfilesMessage.class);
            if (knownProfiles != null) {
                for (final Sample sample : knownProfiles) {
                    addRareAlleles(sample, popstats, rareAlleles);
                }
            }
        }
        return rareAlleles;
    }

    private void addRareAlleles(final Sample sample, final PopulationStatistics popstats, final Collection<Allele> rareAlleles) {
        final Collection<String> enabledLoci = _messageBus.query(EnabledLociMessage.class);
        if (sample.isEnabled()) {
            for (final Locus locus : sample.getLoci()) {
                if (enabledLoci.contains(locus.getName())) {
                    for (final Allele allele : locus.getAlleles()) {
                        if (popstats.isRareAllele(allele)) {
                            rareAlleles.add(allele);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the duration for the search.
     *
     * @return the duration of the search in milliseconds.
     */
    long getDuration() {
        return _duration;
    }

    /**
     * Gets the filename of the case log.
     *
     * @return A string containing the name of the logfile generated for the search.
     */
    public String getFilename() {
        return _logFileName;
    }

    public void logExcludedProfiles(final SearchResults results) {
        _caseLogger.info("=================");
        _caseLogger.info("Excluded Profiles");
        if (results.getExcludedProfiles().size() > 0) {
            for (final ExclusionReason reason : ExclusionReason.values()) {
                final List<String> excludedProfileNames = results.getExcludedProfileNames(reason);
                if (excludedProfileNames.size() > 0) {
                    Collections.sort(excludedProfileNames);
                    _caseLogger.info("  Problem:           {}", reason.getDescription());
                    _caseLogger.info("  Profiles excluded: {}", excludedProfileNames.size());
                    StringBuilder lineBuilder = new StringBuilder();
                    for (int idx = 0; idx < Math.min(MAX_REPORTED_EXCLUDED_PROFILE_NAMES, excludedProfileNames.size()); idx++) {
                        if (idx % 10 == 0 && idx > 0) {
                            _caseLogger.info("    " + lineBuilder.toString());
                            lineBuilder = new StringBuilder();
                        }

                        lineBuilder.append(addPadding(excludedProfileNames.get(idx), 15)).append("  ");
                    }
                    if (lineBuilder.length() > 0) {
                        _caseLogger.info("    " + lineBuilder.toString());
                    }
                    if (excludedProfileNames.size() > MAX_REPORTED_EXCLUDED_PROFILE_NAMES) {
                        _caseLogger.info("    and {} more.", excludedProfileNames.size() - MAX_REPORTED_EXCLUDED_PROFILE_NAMES);
                    }
                }
            }
        }
        else {
            _caseLogger.info("  All profiles in the database were evaluated, no profiles were excluded.");
        }
    }

    public long getStartTime() {
        return _startTime;
    }
}
