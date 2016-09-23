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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.analysis.parameterestimation.DropoutEstimation;
import nl.minvenj.nfi.smartrank.analysis.parameterestimation.DropoutEstimator;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.Contributor;
import nl.minvenj.nfi.smartrank.domain.DNADatabase;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.LikelihoodRatio;
import nl.minvenj.nfi.smartrank.domain.LocusLikelihoods;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.io.samples.SampleWriter;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.messages.data.CrimeSceneProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseMessage;
import nl.minvenj.nfi.smartrank.messages.data.DefenseHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.EnabledLociMessage;
import nl.minvenj.nfi.smartrank.messages.data.LikelihoodRatiosMessage;
import nl.minvenj.nfi.smartrank.messages.data.ProsecutionHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.SearchResultsMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.messages.status.DetailStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.PercentReadyMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchAbortedMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchCompletedMessage;
import nl.minvenj.nfi.smartrank.model.StatisticalModel;
import nl.minvenj.nfi.smartrank.model.smartrank.SmartRankModel;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.NullUtils;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.report.jasper.ReportGenerator;
import nl.minvenj.nfi.smartrank.utils.FileNameSanitizer;

public class SmartRankAnalysis extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(SmartRankAnalysis.class);
    private final CaseLogger _caseLogger;
    private boolean _interrupted;
    private StatisticalModel _model;
    private final MessageBus _messageBus;

    public SmartRankAnalysis() {
        setName("SmartRankAnalysis");
        _messageBus = MessageBus.getInstance();
        _messageBus.send(this, new SearchResultsMessage(null));
        try {
            _caseLogger = new CaseLogger();
        }
        catch (final Throwable t) {
            LOG.error("Could not create case logger!", t);
            throw t;
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        LOG.info("Interrupted!");
        _interrupted = true;
        _model.interrupt();
    }

    @Override
    public void run() {
        ProsecutionHypothesis hp = null;
        DefenseHypothesis hd = null;
        int specimenIndex = 0;
        int analysedSpecimens = 0;
        int percentReady = 0;

        final DNADatabase db = _messageBus.query(DatabaseMessage.class);
        final Iterator<Sample> samplesIterator = db.iterator();
        boolean headerLogged = false;

        final SearchResults searchResults = new SearchResults(db.getRecordCount());
        searchResults.setLogFileName(_caseLogger.getFilename());
        searchResults.addExcludedProfiles(db.getBadRecordList());

        try {
            _messageBus.send(this, new LikelihoodRatiosMessage(new ArrayList<LikelihoodRatio>()));
            final AnalysisParameters parameters = _messageBus.query(AnalysisParametersMessage.class);
            searchResults.setParameters(parameters);

            hp = _messageBus.query(ProsecutionHypothesisMessage.class);
            hd = _messageBus.query(DefenseHypothesisMessage.class);

            _model = new SmartRankModel();

            if (SmartRankRestrictions.isAutomaticParameterEstimationEnabled() || parameters.isAutomaticParameterEstimationToBePerformed()) {
                final DropoutEstimator estimator = new DropoutEstimator(null);
                final Collection<String> enabledLoci = _messageBus.query(EnabledLociMessage.class);
                final Collection<Sample> crimesceneProfiles = _messageBus.query(CrimeSceneProfilesMessage.class);
                _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.ESTIMATING_DROPOUT));
                final DropoutEstimation estimation = estimator.estimate(hd, enabledLoci, crimesceneProfiles);
                searchResults.getParameters().setDropoutEstimation(estimation);
                final double dropout = estimation.getMaximum().doubleValue();
                setDropout(hd, dropout);
                setDropout(hp, dropout);
            }
            if (searchResults.getParameters().getDropoutEstimation() != null) {
                _caseLogger.logHeader(_model.getModelName(), searchResults.getParameters().getDropoutEstimation());
            }
            else {
                _caseLogger.logHeader(_model.getModelName());
            }

            _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.SEARCHING));
            _messageBus.send(this, new PercentReadyMessage(0));

            _caseLogger.logHypothesis(hp);
            _caseLogger.logHypothesis(hd);
            headerLogged = true;

            searchResults.setProsecution(hp);
            searchResults.setDefense(hd);

            LocusLikelihoods prD = null;
            if (parameters.isCalculateHdOnce()) {
                _messageBus.send(this, new DetailStringMessage("Calculating Pr(E|Hd)"));
                prD = _model.calculateLikelihood(hd, parameters);
                _model.reset();
            }

            _messageBus.send(this, new SearchResultsMessage(searchResults));

            final int minimumNumberOfLoci = SmartRankRestrictions.getMinimumNumberOfLoci();
            while (!_interrupted && !isInterrupted() && samplesIterator.hasNext()) {
                final Sample candidateSample = samplesIterator.next();
                LOG.debug("Handling sample {}", candidateSample);

                _messageBus.send(this, new DetailStringMessage("Calculating Pr(E|Hp) for " + candidateSample));
                percentReady = (specimenIndex++ * 100) / db.getRecordCount();
                _messageBus.send(this, new PercentReadyMessage(percentReady));

                if (getEnabledLocusCount(candidateSample) < minimumNumberOfLoci) {
                    searchResults.addExcludedProfile(candidateSample, ExclusionReason.NOT_ENOUGH_LOCI);
                    continue;
                }

                if (!parameters.isCalculateHdOnce()) {
                    _messageBus.send(this, new DetailStringMessage("Calculating Pr(E|Hd) for " + candidateSample));
                    final Hypothesis realHd = hd.copy();
                    realHd.addNonContributor(candidateSample, hp.getCandidateDropout());
                    prD = _model.calculateLikelihood(realHd, parameters);
                    _model.reset();
                }

                hp.setCandidate(candidateSample);
                analysedSpecimens++;

                LocusLikelihoods prP = null;
                try {
                    prP = _model.calculateLikelihood(hp, parameters);
                }
                catch (final Throwable t) {
                    LOG.error("Error evaluating candidate {}!", candidateSample.getName(), t);
                }
                if (prP != null) {
                    final LikelihoodRatio lr = new LikelihoodRatio(candidateSample, prP, prD);
                    LOG.debug(" {} = {}", candidateSample, lr.getOverallRatio());
                    updateResults(candidateSample, searchResults, lr);
                }
                _model.reset();
            }

            if (isInterrupted() || _interrupted) {
                throw new InterruptedException();
            }

            _caseLogger.logExcludedProfiles(searchResults);
            _caseLogger.logRanking();
            _caseLogger.logFooter(analysedSpecimens);
            searchResults.setDuration(_caseLogger.getDuration());
            searchResults.setSucceeded();

            hp.reset();

            final String reportName = new ReportGenerator().generateAndWait(_caseLogger.getStartTime());
            searchResults.setReportName(reportName);

            if (SmartRankRestrictions.isProfileExportEnabled()) {
                final Thread t = new Thread() {
                    @Override
                    public void run() {
                        final File outputFolder = new File(reportName).getParentFile();
                        String profileName = "";
                        try {
                            final List<LikelihoodRatio> positiveLRs = new ArrayList<>(searchResults.getPositiveLRs());
                            Collections.sort(positiveLRs, new Comparator<LikelihoodRatio>() {
                                @Override
                                public int compare(final LikelihoodRatio o1, final LikelihoodRatio o2) {
                                    return -o1.compareTo(o2);
                                }
                            });

                            final int total = positiveLRs.size();
                            boolean overThreshold = true;
                            for (int idx = 0; idx < Math.min(total, searchResults.getParameters().getMaximumNumberOfResults()) && overThreshold; idx++) {
                                final LikelihoodRatio lr = positiveLRs.get(idx);
                                final Sample profile = lr.getProfile();

                                if (lr.getOverallRatio().getRatio() < searchResults.getParameters().getLrThreshold()) {
                                    overThreshold = false;
                                    break;
                                }

                                _messageBus.send(this, new PercentReadyMessage((idx * 100) / total));
                                _messageBus.send(this, new DetailStringMessage("Exporting profile " + profileName));
                                profileName = profile.getName();
                                new SampleWriter(new File(outputFolder, FileNameSanitizer.sanitize(profile.getName()) + ".csv")).write(profile);
                            }
                        }
                        catch (final Throwable e) {
                            LOG.error("Error saving profile after search: profile {} failed.", profileName, e);
                            _messageBus.send(this, new ErrorStringMessage("Error saving profile after search. profile " + profileName + " failed: " + e.getMessage()));
                        }
                    };
                };
                t.start();
                t.join();
            }
            _messageBus.send(this, new SearchCompletedMessage(searchResults));
        }
        catch (final InterruptedException ie) {
            _caseLogger.logExcludedProfiles(searchResults);
            _caseLogger.logRanking();
            _caseLogger.logFooter(ie, percentReady, analysedSpecimens);
            searchResults.setFailed(ie);
            _messageBus.send(this, new SearchAbortedMessage(searchResults));
        }
        catch (final Throwable e) {
            if (!headerLogged) {
                _caseLogger.logHeader(_model == null ? "" : _model.getModelName());
                _caseLogger.logHypothesis(hp);
                _caseLogger.logHypothesis(hd);
            }
            _caseLogger.logExcludedProfiles(searchResults);
            _caseLogger.logRanking();
            _caseLogger.logFooter(e);
            searchResults.setFailed(e);
            _messageBus.send(this, new SearchAbortedMessage(searchResults));
        }
        finally {
            if (hp != null) {
                hp.reset();
            }
            _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.READY_FOR_ANALYSIS));
        }
    }

    private int getEnabledLocusCount(final Sample profile) {
        int count = 0;
        final Collection<String> enabledLoci = MessageBus.getInstance().query(EnabledLociMessage.class);
        for (final String locus : enabledLoci) {
            if (profile.hasLocus(locus)) {
                count++;
            }
        }
        return count;
    }

    private void updateResults(final Sample candidateSample, final SearchResults searchResults, final LikelihoodRatio lr) {
        searchResults.addLR(lr);
        if (SmartRankRestrictions.isAllLRsStored() || lr.getOverallRatio().getRatio() > 1) {
            if (lr.getOverallRatio().getRatio() > searchResults.getParameters().getLrThreshold()) {
                _caseLogger.logResult(candidateSample, lr);
            }
            final List<LikelihoodRatio> lrs = new ArrayList<>(NullUtils.getValue(_messageBus.query(LikelihoodRatiosMessage.class), new ArrayList<LikelihoodRatio>()));
            lrs.add(lr);
            Collections.sort(lrs, new Comparator<LikelihoodRatio>() {
                @Override
                public int compare(final LikelihoodRatio o1, final LikelihoodRatio o2) {
                    return -o1.compareTo(o2);
                }
            });

            _messageBus.send(this, new LikelihoodRatiosMessage(lrs));
        }
    }

    private void setDropout(final Hypothesis hypothesis, final double dropout) {
        hypothesis.setUnknownDropoutProbability(dropout);
        for (final Contributor contributor : hypothesis.getContributors()) {
            if (contributor.isCandidate()) {
                contributor.setDropoutProbability(dropout);
            }
            else {
                contributor.setDropoutProbability(0.01);
            }
        }
    }
}
