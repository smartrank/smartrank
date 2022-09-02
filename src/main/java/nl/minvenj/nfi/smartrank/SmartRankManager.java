/*
 * Copyright (C) 2015,2019 Netherlands Forensic Institute
 *
 * "SmartRankManager" program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * "SmartRankManager" program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "SmartRankManager" program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.minvenj.nfi.smartrank;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.analysis.SmartRankAnalysis;
import nl.minvenj.nfi.smartrank.analysis.parameterestimation.DropoutEstimation;
import nl.minvenj.nfi.smartrank.analysis.parameterestimation.DropoutEstimator;
import nl.minvenj.nfi.smartrank.analysis.parameterestimation.DropoutProgressListener;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.Contributor;
import nl.minvenj.nfi.smartrank.domain.DNADatabase;
import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.domain.ProblemLocation;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.gui.TimeUpdater;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseValidationEventListener;
import nl.minvenj.nfi.smartrank.io.samples.SampleReader;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReaderFactory;
import nl.minvenj.nfi.smartrank.io.searchcriteria.locim.SmartRankImportFileReader;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.SmartRankImportFile;
import nl.minvenj.nfi.smartrank.io.statistics.StatisticsReader;
import nl.minvenj.nfi.smartrank.messages.commands.ClearSearchCriteriaMessage;
import nl.minvenj.nfi.smartrank.messages.commands.EstimateDropoutMessage;
import nl.minvenj.nfi.smartrank.messages.commands.RemoveCrimeSceneProfiles;
import nl.minvenj.nfi.smartrank.messages.commands.RemoveKnownProfiles;
import nl.minvenj.nfi.smartrank.messages.commands.StartAnalysisCommand;
import nl.minvenj.nfi.smartrank.messages.commands.UpdateCrimeSceneProfiles;
import nl.minvenj.nfi.smartrank.messages.commands.UpdateKnownProfiles;
import nl.minvenj.nfi.smartrank.messages.data.AddCrimeSceneFilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.AddKnownFilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.messages.data.CrimeSceneProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseConnectionMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseFileMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseMessage;
import nl.minvenj.nfi.smartrank.messages.data.DefenseHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.DropinMessage;
import nl.minvenj.nfi.smartrank.messages.data.EnabledLociMessage;
import nl.minvenj.nfi.smartrank.messages.data.KnownProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.LRThresholdMessage;
import nl.minvenj.nfi.smartrank.messages.data.LoadSearchCriteriaMessage;
import nl.minvenj.nfi.smartrank.messages.data.OutputLocationMessage;
import nl.minvenj.nfi.smartrank.messages.data.PopulationStatisticsFileMessage;
import nl.minvenj.nfi.smartrank.messages.data.PopulationStatisticsMessage;
import nl.minvenj.nfi.smartrank.messages.data.ProsecutionHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.RareAlleleFrequencyMessage;
import nl.minvenj.nfi.smartrank.messages.data.ReportTopMessage;
import nl.minvenj.nfi.smartrank.messages.data.ThetaMessage;
import nl.minvenj.nfi.smartrank.messages.data.UpdateDefenseContributorMessage;
import nl.minvenj.nfi.smartrank.messages.data.UpdateDefenseNoncontributorMessage;
import nl.minvenj.nfi.smartrank.messages.data.UpdateDefenseUnknownsMessage;
import nl.minvenj.nfi.smartrank.messages.data.UpdateProsecutionContributorMessage;
import nl.minvenj.nfi.smartrank.messages.data.UpdateProsecutionNoncontributorMessage;
import nl.minvenj.nfi.smartrank.messages.data.UpdateProsecutionUnknownsMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.messages.status.DatabaseFormatProblemMessage;
import nl.minvenj.nfi.smartrank.messages.status.DetailStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.PercentReadyMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchAbortedMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchCompletedMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.NullUtils;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.raven.numberformat.NumberUtils;
import nl.minvenj.nfi.smartrank.raven.timeformat.TimeUtils;
import nl.minvenj.nfi.smartrank.utils.OrderMergedList;

/**
 * Contains the business logic for the SmartRank application.
 *
 * @author Netherlands Forensic Institute
 */
public class SmartRankManager {

    private static final Logger LOG = LoggerFactory.getLogger(SmartRankManager.class);

    private static SmartRankManager _me;
    private Thread _currentTask;

    private final MessageBus _messageBus;

    private SmartRankManager() {
        _messageBus = MessageBus.getInstance();
        _messageBus.registerSubscriber(this);
        _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.WAIT_DB));
        _messageBus.send(this, new AnalysisParametersMessage(new AnalysisParameters()));
    }

    public static synchronized SmartRankManager getInstance() {
        if (_me == null) {
            _me = new SmartRankManager();
        }
        return _me;
    }

    public synchronized MessageBus getMessageBus() {
        return _messageBus;
    }

    public synchronized Thread getCurrentTask() {
        return _currentTask;
    }

    @RavenMessageHandler(DatabaseFileMessage.class)
    public void onNewDatabase(final File dbFile) {
        LOG.debug("Loading new database: {}", dbFile);
        if (dbFile != null) {
            _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.VERIFYING_DB));
            _messageBus.send(this, new DetailStringMessage(""));
            try {
                final DNADatabase database = new DNADatabase(dbFile);
                database.validate(new DatabaseValidationEventListener() {
                    @Override
                    public void onProgress(final long current, final long max) {
                        // TODO: SMARTRANK-235: Make percentage calculations more robust
                        final int percentReady = (int) ((current * 100) / max);
                        _messageBus.send(database, new PercentReadyMessage(percentReady));
                    }

                    @Override
                    public void onProblem(final String specimen, final String locus, final String message) {
                        LOG.error("Problem {} {} {}", specimen, locus, message);
                        _messageBus.send(database, new DatabaseFormatProblemMessage(new ProblemLocation(specimen, locus, message)));
                    }
                });
                _messageBus.send(this, new DatabaseMessage(database));
                LOG.debug("Loading succeeded. File hash {}", database.getFileHash());
            }
            catch (final IllegalArgumentException ex) {
                LOG.error("Error loading database file ", ex);
                _messageBus.send(this, new ErrorStringMessage(ex.getLocalizedMessage()));
                _messageBus.send(this, new DatabaseMessage(null));
            }
            catch (final Throwable ex) {
                LOG.error("Error loading database file ", ex);
                _messageBus.send(this, new ErrorStringMessage(ex.getClass().getName() + " - " + ex.getLocalizedMessage()));
                _messageBus.send(this, new DatabaseMessage(null));
            }
            finally {
                setApplicationStatus();
            }
        }
    }

    @RavenMessageHandler(DatabaseConnectionMessage.class)
    public void onNewDatabaseConnection(final DatabaseConfiguration config) {
        LOG.debug("Connecting to database at {}", config.getConnectString());
        _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.VERIFYING_DB));
        _messageBus.send(this, new DetailStringMessage(""));
        try {
            final DNADatabase database = new DNADatabase(config);
            database.validate(new DatabaseValidationEventListener() {
                @Override
                public void onProgress(final long current, final long max) {
                    // SMARTRANK-235: Robustify percentage calculations
                    final int percentReady = (int) ((current * 100) / max);
                    _messageBus.send(database, new PercentReadyMessage(percentReady));
                }

                @Override
                public void onProblem(final String specimen, final String locus, final String message) {
                    LOG.error("Problem {} {} {}", specimen, locus, message);
                    _messageBus.send(database, new DatabaseFormatProblemMessage(new ProblemLocation(specimen, locus, message)));
                }
            });
            _messageBus.send(this, new DatabaseMessage(database));
            LOG.debug("Loading succeeded. File hash {}", database.getFileHash());
        }
        catch (final IllegalArgumentException ex) {
            LOG.error("Error building database connection to {}", config.getConnectString(), ex);
            _messageBus.send(this, new ErrorStringMessage(ex.getLocalizedMessage()));
            _messageBus.send(this, new DatabaseMessage(null));
        }
        catch (final Throwable ex) {
            LOG.error("Error building database connection to {}", config.getConnectString(), ex);
            _messageBus.send(this, new ErrorStringMessage(ex.getClass().getName() + " - " + ex.getLocalizedMessage()));
            _messageBus.send(this, new DatabaseMessage(null));
        }
        finally {
            setApplicationStatus();
        }
    }

    @RavenMessageHandler(AddCrimeSceneFilesMessage.class)
    public void onLoadCrimesceneProfiles(final File[] files) {
        LOG.debug("Loading crimescene profiles: {}", Arrays.toString(files));
        try {
            _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.READING_SAMPLES));

            final List<Sample> existingProfiles = _messageBus.query(CrimeSceneProfilesMessage.class);
            final ArrayList<Sample> newProfiles = new ArrayList<>();
            if (existingProfiles != null) {
                newProfiles.addAll(existingProfiles);
            }

            String msg = "";
            for (final File file : files) {
                final SampleReader reader = new SampleReader(file);
                try {
                    for (final Sample sample : reader.getSamples()) {
                        sample.setEnabled(SmartRankGUISettings.isNewCrimesceneProfilesEnabled());
                        newProfiles.add(sample);
                    }
                    LOG.debug("CrimeScene file: '{}', Hash: '{}'", reader.getFile(), reader.getFileHash());
                }
                catch (final Exception ex) {
                    if (ex.getMessage() != null) {
                        LOG.error("Could not read file {}: {}", reader.getFile(), ex.getMessage());
                        msg += "  <b>" + reader.getFile().getName() + "</b>: " + ex.getMessage() + "<br>";
                    }
                    else {
                        LOG.error("Could not read file {}: {}", reader.getFile(), ex.getClass().getSimpleName(), ex);
                        msg += "  <b>" + reader.getFile().getName() + "</b>: " + ex.getClass().getSimpleName() + "<br>";
                    }
                }
            }

            if (!msg.isEmpty()) {
                final String errorMsg = "<html>Could not read file" + (files.length > 1 ? "s" : "") + ":<br>" + msg;
                _messageBus.send(this, new ErrorStringMessage(errorMsg));
            }

            final DefenseHypothesis def = getDefenseHypothesis();
            _messageBus.send(this, new DefenseHypothesisMessage(def));

            final ProsecutionHypothesis pro = getProsecutionHypothesis();
            _messageBus.send(this, new ProsecutionHypothesisMessage(pro));

            _messageBus.send(this, new CrimeSceneProfilesMessage(newProfiles));
            updateEnabledLoci();

            final AnalysisParameters parms = _messageBus.query(AnalysisParametersMessage.class);
            parms.setEnabledCrimesceneProfiles(newProfiles);
            parms.setDropoutEstimation(null);
        }
        catch (final IOException ex) {
            _messageBus.send(this, new ErrorStringMessage(ex.getClass().getName() + " - " + ex.getLocalizedMessage()));
        }
        finally {
            setApplicationStatus();
        }
    }

    @RavenMessageHandler(RemoveCrimeSceneProfiles.class)
    public void onRemoveCrimesceneProfile(final List<Sample> profiles) {
        LOG.debug("Removing crimescene profile {}", profiles);
        final List<Sample> curSamples = _messageBus.query(CrimeSceneProfilesMessage.class);
        curSamples.removeAll(profiles);
        _messageBus.send(this, new CrimeSceneProfilesMessage(curSamples));

        final AnalysisParameters parms = _messageBus.query(AnalysisParametersMessage.class);
        parms.setEnabledCrimesceneProfiles(curSamples);
        parms.setDropoutEstimation(null);

        updateEnabledLoci();
        setApplicationStatus();
    }

    @RavenMessageHandler(UpdateCrimeSceneProfiles.class)
    public void onCrimesceneProfilesUpdated(final List<Sample> samples) {
        LOG.debug("Updated crimescene profiles {}", samples);

        final AnalysisParameters parms = _messageBus.query(AnalysisParametersMessage.class);
        final Collection<Sample> profiles = _messageBus.query(CrimeSceneProfilesMessage.class);
        parms.setEnabledCrimesceneProfiles(profiles);
        parms.setDropoutEstimation(null);

        setApplicationStatus();
    }

    @RavenMessageHandler(AddKnownFilesMessage.class)
    public void onLoadKnownProfiles(final File[] files) {
        LOG.debug("Load known profiles {}", Arrays.toString(files));
        try {
            final DefenseHypothesis defense = getDefenseHypothesis();
            final ProsecutionHypothesis prosecution = getProsecutionHypothesis();

            _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.READING_SAMPLES));
            final List<Sample> existingProfiles = _messageBus.query(KnownProfilesMessage.class);
            final ArrayList<Sample> allProfiles = new ArrayList<>();
            if (existingProfiles != null) {
                allProfiles.addAll(existingProfiles);
            }

            String msg = "";
            for (final File file : files) {
                final SampleReader reader = new SampleReader(file);
                try {
                    final Collection<Sample> newProfiles = reader.getSamples();
                    LOG.debug("KnownProfiles file: '{}', Hash: '{}'", reader.getFile(), reader.getFileHash());
                    for (final Sample sample : newProfiles) {
                        sample.setEnabled(SmartRankGUISettings.isNewKnownProfilesEnabled());
                        if (sample.isEnabled()) {
                            prosecution.addContributor(sample, SmartRankRestrictions.getDropoutDefault());
                            defense.addContributor(sample, SmartRankRestrictions.getDropoutDefault());
                        }
                    }
                    allProfiles.addAll(newProfiles);
                }
                catch (final Exception ex) {
                    if (ex.getMessage() != null) {
                        LOG.error("Could not read file {}: {}", reader.getFile(), ex.getMessage());
                        msg += "  <b>" + reader.getFile().getName() + "</b>: " + ex.getMessage() + "<br>";
                    }
                    else {
                        LOG.error("Could not read file {}: {}", reader.getFile(), ex.getClass().getSimpleName(), ex);
                        msg += "  <b>" + reader.getFile().getName() + "</b>: " + ex.getClass().getSimpleName() + "<br>";
                    }
                }
            }

            if (!msg.isEmpty()) {
                final String errorMsg = "<html>Could not read file" + (files.length > 1 ? "s" : "") + ":<br>" + msg;
                _messageBus.send(this, new ErrorStringMessage(errorMsg));
            }

            final AnalysisParameters parms = _messageBus.query(AnalysisParametersMessage.class);
            parms.setDropoutEstimation(null);

            _messageBus.send(this, new KnownProfilesMessage(allProfiles));
            _messageBus.send(this, new ProsecutionHypothesisMessage(prosecution));
            _messageBus.send(this, new DefenseHypothesisMessage(defense));
        }
        catch (final IOException ex) {
            _messageBus.send(this, new ErrorStringMessage(ex.getClass().getName() + " - " + ex.getLocalizedMessage()));
        }
        finally {
            updateEnabledLoci();
            setApplicationStatus();
        }
    }

    @RavenMessageHandler(ReportTopMessage.class)
    public void onReportTopChanged(final int top) {
        LOG.debug("Setting max reported results to {}", top);
        final AnalysisParameters parms = _messageBus.query(AnalysisParametersMessage.class);
        parms.setMaxReturnedResults(top);
    }

    @RavenMessageHandler(RareAlleleFrequencyMessage.class)
    public void onUpdateRareAlleleFrequency(final double frequency) {
        LOG.debug("onChangedRareAlleleFrequency {}", frequency);
        final PopulationStatistics popstats = _messageBus.query(PopulationStatisticsMessage.class);
        if (popstats != null) {
            popstats.setRareAlleleFrequency(frequency);
        }

        final DefenseHypothesis defense = getDefenseHypothesis();
        final ProsecutionHypothesis prosecution = getProsecutionHypothesis();
        _messageBus.send(this, new ProsecutionHypothesisMessage(prosecution));
        _messageBus.send(this, new DefenseHypothesisMessage(defense));
    }

    @RavenMessageHandler(RemoveKnownProfiles.class)
    public void onRemoveKnownProfiles(final List<Sample> profiles) {
        LOG.debug("Removing known profile {}", profiles);
        final List<Sample> curSamples = _messageBus.query(KnownProfilesMessage.class);
        curSamples.removeAll(profiles);

        final DefenseHypothesis defense = getDefenseHypothesis();
        final ProsecutionHypothesis prosecution = getProsecutionHypothesis();

        for (final Sample s : profiles) {
            defense.removeContributor(s);
            prosecution.removeContributor(s);
        }

        _messageBus.send(this, new KnownProfilesMessage(curSamples));
        _messageBus.send(this, new ProsecutionHypothesisMessage(prosecution));
        _messageBus.send(this, new DefenseHypothesisMessage(defense));

        final AnalysisParameters parms = _messageBus.query(AnalysisParametersMessage.class);
        parms.setDropoutEstimation(null);

        updateEnabledLoci();
        setApplicationStatus();
    }

    @RavenMessageHandler(UpdateKnownProfiles.class)
    public void onKnownProfileUpdated(final List<Sample> samples) {
        LOG.debug("Update known profile {}", samples);

        final DefenseHypothesis defense = getDefenseHypothesis();
        final ProsecutionHypothesis prosecution = getProsecutionHypothesis();

        for (final Sample s : samples) {
            if (s.isEnabled()) {
                defense.addContributor(s, SmartRankRestrictions.getDropoutDefault());
                prosecution.addContributor(s, SmartRankRestrictions.getDropoutDefault());
            }
            else {
                defense.removeContributor(s);
                prosecution.removeContributor(s);
            }
        }

        _messageBus.send(this, new ProsecutionHypothesisMessage(prosecution));
        _messageBus.send(this, new DefenseHypothesisMessage(defense));

        final AnalysisParameters parms = _messageBus.query(AnalysisParametersMessage.class);
        parms.setDropoutEstimation(null);

        setApplicationStatus();
    }

    @RavenMessageHandler(SearchCompletedMessage.class)
    public void onSearchCompleted(final SearchResults results) {
        LOG.debug("Search completed after {}", TimeUtils.formatDuration(results.getDuration()));
        LOG.debug("Logfile location: {}", results.getLogFileName());
        LOG.debug("Report location: {}", results.getReportFileName());
    }

    @RavenMessageHandler(SearchAbortedMessage.class)
    public void onSearchAborted(final SearchResults results) {
        LOG.debug("Search {} after {}", (results.isSucceeded() ? "completed" : "aborted"), TimeUtils.formatDuration(results.getDuration()));
        LOG.debug("Logfile location: {}", results.getLogFileName());
    }

    @RavenMessageHandler(StartAnalysisCommand.class)
    public void onStartAnalysis() {
        LOG.debug("Start analysis command received");

        int seconds = 10;
        while (seconds > 0 && _currentTask != null && _currentTask.isAlive()) {
            LOG.debug("Waiting for current task to finish ({})", _currentTask.getName());
            try {
                Thread.sleep(1000);
            }
            catch (final InterruptedException e) {
                final SearchResults result = new SearchResults(_messageBus.query(DatabaseMessage.class).getConfiguration());
                result.setFailed(new InterruptedException("Interrupted while waiting for current task to finish!"));
                _messageBus.send(this, new SearchAbortedMessage(result));
                return;
            }
            seconds--;
        }

        if (_currentTask != null && _currentTask.isAlive()) {
            LOG.debug("Interrupting current task ({})", _currentTask.getName());
            _currentTask.interrupt();
            while (_currentTask.isAlive()) {
                try {
                    Thread.sleep(10);
                }
                catch (final InterruptedException e) {
                }
            }
            LOG.debug("Current task interrupted ({})", _currentTask.getName());
        }

        if (_currentTask == null || !_currentTask.isAlive()) {
            // Revalidate the database if required
            final DNADatabase database = _messageBus.query(DatabaseMessage.class);
            try {
                LOG.debug("Revalidating database");
                _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.VERIFYING_DB));
                database.revalidate(new DatabaseValidationEventListener() {
                    @Override
                    public void onProgress(final long current, final long max) {
                        final int percentReady = (int) ((current * 100L) / max);
                        _messageBus.send(database, new PercentReadyMessage(percentReady));
                    }

                    @Override
                    public void onProblem(final String specimen, final String locus, final String message) {
                        LOG.error("Problem {} {} {}", specimen, locus, message);
                        _messageBus.send(database, new DatabaseFormatProblemMessage(new ProblemLocation(specimen, locus, message)));
                    }
                });
                LOG.debug("Database revalidated");
                LOG.debug("Initializing search");
                _currentTask = new SmartRankAnalysis();
                LOG.debug("Starting search");
                _currentTask.start();
            }
            catch (final IOException | InterruptedException e) {
                LOG.error("Error revalidating the database!", e);
                final SearchResults result = new SearchResults(_messageBus.query(DatabaseMessage.class).getConfiguration());
                result.setFailed(new IllegalArgumentException("Error revalidating the database!", e));
                _messageBus.send(this, new SearchAbortedMessage(result));
            }
        }
        else {
            LOG.error("Could not start search!");
            final SearchResults result = new SearchResults(_messageBus.query(DatabaseMessage.class).getConfiguration());
            result.setFailed(new IllegalArgumentException("Could not start search!"));
            _messageBus.send(this, new SearchAbortedMessage(result));
        }
    }

    @RavenMessageHandler(PopulationStatisticsFileMessage.class)
    public boolean onNewPopulationStatistics(final String popStats) {
        boolean returnValue = false;
        LOG.debug("New population statistics {}", popStats);
        try {
            _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.LOADING_POPULATION_STATISTICS));
            final StatisticsReader reader = new StatisticsReader(new File(popStats));
            final PopulationStatistics stats = reader.getStatistics();
            stats.setRareAlleleFrequency(NullUtils.getValue(_messageBus.query(RareAlleleFrequencyMessage.class), 0.0003));
            final DefenseHypothesis def = getDefenseHypothesis();
            final ProsecutionHypothesis pro = getProsecutionHypothesis();
            def.setStatistics(stats);
            pro.setStatistics(stats);
            LOG.debug("Population statistics loaded. Hash: {}", stats.getFileHash());
            _messageBus.send(this, new PopulationStatisticsMessage(stats));
            _messageBus.send(this, new ProsecutionHypothesisMessage(pro));
            _messageBus.send(this, new DefenseHypothesisMessage(def));
            updateEnabledLoci();
            returnValue = true;
        }
        catch (final MalformedURLException ex) {
            _messageBus.send(this, new ErrorStringMessage(ex.getLocalizedMessage()));
        }
        catch (final IOException ex) {
            _messageBus.send(this, new ErrorStringMessage(ex.getLocalizedMessage()));
        }
        finally {
            setApplicationStatus();
        }
        return returnValue;
    }

    @RavenMessageHandler(EstimateDropoutMessage.class)
    public void onEstimateDropout() {
        LOG.debug("Estimating Dropout parameters");
        try {
            _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.ESTIMATING_DROPOUT));
            final DefenseHypothesis hd = getDefenseHypothesis();
            final ProsecutionHypothesis hp = getProsecutionHypothesis();
            final DropoutEstimator estimator = new DropoutEstimator(new DropoutProgressListener() {
                private final AtomicInteger _ticks = new AtomicInteger();
                private int _max;

                @Override
                public void onIterationDone() {
                    final int percent = (_ticks.incrementAndGet() * 100) / _max;
                    _messageBus.send(this, new PercentReadyMessage(percent));
                }

                @Override
                public void setIterations(final int iterations) {
                    _max = iterations;
                }
            });
            _currentTask = estimator;
            final Collection<String> enabledLoci = _messageBus.query(EnabledLociMessage.class);
            final Collection<Sample> crimesceneProfiles = _messageBus.query(CrimeSceneProfilesMessage.class);
            final DropoutEstimation estimation = estimator.estimate(hd, enabledLoci, crimesceneProfiles);
            LOG.debug("Dropout estimation: {}", estimation);
            final double dropout = estimation.getEstimatedDropout().doubleValue();

            if (dropout > SmartRankRestrictions.getDropoutMaximum()) {
                _messageBus.send(this, new ErrorStringMessage(String.format("The result of the dropout estimation (%s) exceeds the maximum allowed value for dropout (%s)!\nEstimation result cannot be used.", NumberUtils.formatNoExponent(2, dropout), NumberUtils.formatNoExponent(2, SmartRankRestrictions.getDropoutMaximum()))));
                return;
            }

            if (dropout < SmartRankRestrictions.getDropoutMinimum()) {
                _messageBus.send(this, new ErrorStringMessage(String.format("The result of the dropout estimation (%s) falls below the minimum allowed value for dropout (%s)!\nEstimation result cannot be used.", NumberUtils.formatNoExponent(2, dropout), NumberUtils.formatNoExponent(2, SmartRankRestrictions.getDropoutMinimum()))));
                return;
            }

            setDropout(hd, dropout);
            setDropout(hp, dropout);

            final AnalysisParameters parms = _messageBus.query(AnalysisParametersMessage.class);
            parms.setDropoutEstimation(estimation);

            _messageBus.send(this, new ProsecutionHypothesisMessage(hp));
            _messageBus.send(this, new DefenseHypothesisMessage(hd));
        }
        catch (final Throwable t) {
            _messageBus.send(this, new ErrorStringMessage(t.getLocalizedMessage()));
        }
        finally {
            setApplicationStatus();
        }
    }

    public void setDropout(final Hypothesis hypothesis, final double dropout) {
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

    /**
     * Can be called to stop the current analysis and return the application to a passive state.
     */
    public void onStopAnalysis() {
        LOG.debug("Stopping analysis");
        if (_currentTask != null) {
            LOG.debug("Interrupting current task ({})", _currentTask.getName());
            _currentTask.interrupt();
            while (_currentTask.isAlive()) {
                try {
                    Thread.sleep(10);
                }
                catch (final InterruptedException e) {
                }
            }
        }
        TimeUpdater.getInstance().interrupt();
        setApplicationStatus();
    }

    @RavenMessageHandler(UpdateDefenseContributorMessage.class)
    public void onUpdateDefenseContributor(final Contributor contributor) {
        LOG.debug("Updating defense contributor {}", contributor);
        final DefenseHypothesis defense = getDefenseHypothesis();
        defense.addContributor(contributor.getSample(), contributor.getDropoutProbability());
        _messageBus.send(this, new DefenseHypothesisMessage(defense));
        setApplicationStatus();
    }

    @RavenMessageHandler(UpdateDefenseNoncontributorMessage.class)
    public void onUpdateDefenseNonContributor(final Contributor contributor) {
        LOG.debug("Updating defense non-constributor {}", contributor);
        final DefenseHypothesis defense = getDefenseHypothesis();
        defense.addNonContributor(contributor.getSample(), contributor.getDropoutProbability());
        _messageBus.send(this, new DefenseHypothesisMessage(defense));
        setApplicationStatus();
    }

    @RavenMessageHandler(UpdateProsecutionContributorMessage.class)
    public void onUpdateProsecutionContributor(final Contributor contributor) {
        LOG.debug("Updating prosecution contributor {}", contributor);
        final ProsecutionHypothesis prosecution = getProsecutionHypothesis();
        prosecution.addContributor(contributor.getSample(), contributor.getDropoutProbability());
        _messageBus.send(this, new ProsecutionHypothesisMessage(prosecution));
        setApplicationStatus();
    }

    @RavenMessageHandler(UpdateProsecutionNoncontributorMessage.class)
    public void onUpdateProsecutionNonContributor(final Contributor contributor) {
        LOG.debug("Updating prosecution non-contributor {}", contributor);
        final ProsecutionHypothesis prosecution = getProsecutionHypothesis();
        prosecution.addNonContributor(contributor.getSample(), contributor.getDropoutProbability());
        _messageBus.send(this, new ProsecutionHypothesisMessage(prosecution));
        setApplicationStatus();
    }

    @RavenMessageHandler(UpdateProsecutionUnknownsMessage.class)
    public void onUpdateProsecutionUnknowns(final Number[] u) {
        LOG.debug("Updating prosecution unknowns {}", Arrays.toString(u));
        final ProsecutionHypothesis hypo = getProsecutionHypothesis();
        hypo.setUnknownCount(u[0].intValue());
        hypo.setUnknownDropoutProbability(u[1].doubleValue());
        _messageBus.send(this, new ProsecutionHypothesisMessage(hypo));
        setApplicationStatus();
    }

    @RavenMessageHandler(UpdateDefenseUnknownsMessage.class)
    public void onUpdateDefenseUnknowns(final Number[] u) {
        LOG.debug("Updating defense unknowns {}", Arrays.toString(u));
        final DefenseHypothesis hypo = getDefenseHypothesis();
        hypo.setUnknownCount(u[0].intValue());
        hypo.setUnknownDropoutProbability(u[1].doubleValue());
        _messageBus.send(this, new DefenseHypothesisMessage(hypo));
        setApplicationStatus();
    }

    @RavenMessageHandler(LRThresholdMessage.class)
    public void onUpdateLRThreshold(final int lrThreshold) {
        LOG.debug("Updating LR Threshold {}", lrThreshold);
        final AnalysisParameters parms = _messageBus.query(AnalysisParametersMessage.class);
        parms.setLrThreshold(lrThreshold);
    }

    @RavenMessageHandler(DropinMessage.class)
    public void onUpdateDropin(final double dropin) {
        LOG.debug("Updating dropin {}", dropin);
        getDefenseHypothesis().setDropInProbability(dropin);
        getProsecutionHypothesis().setDropInProbability(dropin);
        _messageBus.send(this, new ProsecutionHypothesisMessage(getProsecutionHypothesis()));
        _messageBus.send(this, new DefenseHypothesisMessage(getDefenseHypothesis()));
    }

    @RavenMessageHandler(ThetaMessage.class)
    public void onUpdateTheta(final double theta) {
        LOG.debug("Updating theta {}", theta);
        getDefenseHypothesis().setThetaCorrection(theta);
        getProsecutionHypothesis().setThetaCorrection(theta);
        _messageBus.send(this, new ProsecutionHypothesisMessage(getProsecutionHypothesis()));
        _messageBus.send(this, new DefenseHypothesisMessage(getDefenseHypothesis()));
    }

    @RavenMessageHandler(ClearSearchCriteriaMessage.class)
    public void onClearSearchCriteria() {
        LOG.debug("Clearing Search Criteria");
        _messageBus.send(this, new KnownProfilesMessage(new ArrayList<Sample>()));

        final DefenseHypothesis defenseHypothesis = new DefenseHypothesis();
        defenseHypothesis.setStatistics(_messageBus.query(PopulationStatisticsMessage.class));
        defenseHypothesis.setThetaCorrection(NullUtils.getValue(_messageBus.query(ThetaMessage.class), SmartRankRestrictions.getThetaDefault()));
        defenseHypothesis.setDropInProbability(NullUtils.getValue(_messageBus.query(DropinMessage.class), SmartRankRestrictions.getDropinDefault()));

        final ProsecutionHypothesis prosecutionHypothesis = new ProsecutionHypothesis();
        prosecutionHypothesis.setStatistics(_messageBus.query(PopulationStatisticsMessage.class));
        prosecutionHypothesis.setThetaCorrection(NullUtils.getValue(_messageBus.query(ThetaMessage.class), SmartRankRestrictions.getThetaDefault()));
        prosecutionHypothesis.setDropInProbability(NullUtils.getValue(_messageBus.query(DropinMessage.class), SmartRankRestrictions.getDropinDefault()));

        _messageBus.send(this, new CrimeSceneProfilesMessage(new ArrayList<Sample>()));
        updateEnabledLoci();

        _messageBus.send(this, new LRThresholdMessage(SmartRankRestrictions.getDefaultLRThreshold()));
        _messageBus.send(this, new OutputLocationMessage(""));
        _messageBus.send(this, new DropinMessage(SmartRankRestrictions.getDropinDefault()));
        _messageBus.send(this, new ThetaMessage(SmartRankRestrictions.getThetaDefault()));

        final AnalysisParameters parms = _messageBus.query(AnalysisParametersMessage.class);
        parms.setEnabledCrimesceneProfiles(new ArrayList<Sample>());
        parms.setParameterEstimation(false);
        parms.setMaxReturnedResults(SmartRankRestrictions.getMaximumStoredResults());
        parms.setDropoutEstimation(null);
        parms.setProperties(null);

        _messageBus.send(this, new DefenseHypothesisMessage(defenseHypothesis));
        _messageBus.send(this, new ProsecutionHypothesisMessage(prosecutionHypothesis));
        setApplicationStatus();
    }

    @RavenMessageHandler(LoadSearchCriteriaMessage.class)
    public boolean onLoadSearchCriteria(final File criteriaFile) {
        LOG.debug("Loading Search Criteria from {}", criteriaFile);
        try {
            final boolean retval = onLoadSearchCriteria(SearchCriteriaReaderFactory.getReader(criteriaFile));
            if (!retval) {
                LOG.error("Failed to load search criteria from '" + criteriaFile.getAbsolutePath() + "'");
            }
            return retval;
        }
        catch (final IOException e) {
            LOG.error("Failed to load search criteria from '" + criteriaFile + "'", e);
            _messageBus.send(this, new ErrorStringMessage("<html>Failed to load search criteria:<br>" + e.getClass().getSimpleName() + (e.getLocalizedMessage() == null ? "" : ": " + e.getLocalizedMessage())));
            return false;
        }
    }

    public boolean onLoadSearchCriteria(final String context, final String criteria) {
        NullUtils.argNotNull(criteria, "criteria");
        LOG.debug("Loading Search Criteria from {}", context);
        try {
            final boolean retval = onLoadSearchCriteria(SearchCriteriaReaderFactory.getReader(context, criteria));
            if (!retval) {
                LOG.error("Failed to load search criteria from '" + context + "'");
            }
            return retval;
        }
        catch (final IOException e) {
            LOG.error("Failed to load search criteria from '" + context + "'", e);
            _messageBus.send(this, new ErrorStringMessage("<html>Failed to load search criteria:<br>" + e.getClass().getSimpleName() + (e.getLocalizedMessage() == null ? "" : ": " + e.getLocalizedMessage())));
            return false;
        }
    }

    public boolean onLoadSearchCriteria(final SmartRankImportFile criteria) {
        return onLoadSearchCriteria(new SmartRankImportFileReader(criteria));
    }

    private boolean onLoadSearchCriteria(final SearchCriteriaReader reader) {
        NullUtils.argNotNull(reader, "reader");
        _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.LOADING_SEARCH_CRITERIA));

        try {
            onClearSearchCriteria();

            final PopulationStatistics statistics = reader.getPopulationStatistics();
            if (statistics != null) {
                _messageBus.send(this, new PopulationStatisticsMessage(statistics));
            }

            _messageBus.send(this, new CrimeSceneProfilesMessage(reader.getCrimesceneSamples()));
            updateEnabledLoci();

            final AnalysisParameters parms = _messageBus.query(AnalysisParametersMessage.class);
            parms.setEnabledCrimesceneProfiles(reader.getCrimesceneSamples());
            parms.setParameterEstimation(reader.isAutomaticParameterEstimationToBePerformed());

            if (reader.getMaximumNumberOfResults() != -1) {
                parms.setMaxReturnedResults(reader.getMaximumNumberOfResults());
                _messageBus.send(this, new ReportTopMessage(reader.getMaximumNumberOfResults()));
            }

            if (reader.getLRThreshold() != null) {
                parms.setLrThreshold(reader.getLRThreshold());
                _messageBus.send(this, new LRThresholdMessage(reader.getLRThreshold()));
            }

            parms.setProperties(reader.getProperties());

            final List<Sample> knownProfiles = reader.getKnownProfiles();
            _messageBus.send(this, new KnownProfilesMessage(knownProfiles));

            if (reader.getTheta() != null) {
                _messageBus.send(this, new ThetaMessage(reader.getTheta()));
            }

            if (reader.getDropin() != null) {
                _messageBus.send(this, new DropinMessage(reader.getDropin()));
            }

            updateEnabledLoci();

            final DefenseHypothesis defenseHypothesis = new DefenseHypothesis();
            defenseHypothesis.setStatistics(_messageBus.query(PopulationStatisticsMessage.class));
            defenseHypothesis.setThetaCorrection(_messageBus.query(ThetaMessage.class));
            defenseHypothesis.setDropInProbability(_messageBus.query(DropinMessage.class));
            int hdUnknowns = reader.getHdUnknowns();
            if(hdUnknowns > SmartRankRestrictions.getMaximumUnknownCount()) {
                throw new IllegalArgumentException("Illegal number of unknowns for Hd (" + hdUnknowns + "). SmartRank is configured to accept no more than " + SmartRankRestrictions.getMaximumUnknownCount() + " unknowns");
            }
            defenseHypothesis.setUnknownCount(hdUnknowns);
            defenseHypothesis.setUnknownDropoutProbability(reader.getHdUnknownDropout());

            final ProsecutionHypothesis prosecutionHypothesis = new ProsecutionHypothesis();
            prosecutionHypothesis.setStatistics(_messageBus.query(PopulationStatisticsMessage.class));
            prosecutionHypothesis.setThetaCorrection(_messageBus.query(ThetaMessage.class));
            prosecutionHypothesis.setDropInProbability(_messageBus.query(DropinMessage.class));
            int hpUnknowns = reader.getHpUnknowns();
            if(hpUnknowns > SmartRankRestrictions.getMaximumUnknownCount()) {
                throw new IllegalArgumentException("Illegal number of unknowns for Hp (" + hpUnknowns + "). SmartRank is configured to accept no more than " + SmartRankRestrictions.getMaximumUnknownCount() + " unknowns");
            }
            prosecutionHypothesis.setUnknownCount(hpUnknowns);
            prosecutionHypothesis.setUnknownDropoutProbability(reader.getHpUnknownDropout());

            for (final Sample sample : knownProfiles) {
                final Map<String, Double> hdContributors = reader.getHdContributors();
                final Map<String, Double> hpContributors = reader.getHpContributors();

                if (hdContributors.containsKey(sample.getName())) {
                    defenseHypothesis.addContributor(sample, hdContributors.get(sample.getName()));
                }
                else {
                    defenseHypothesis.addNonContributor(sample, 0);
                }

                if (hpContributors.containsKey(sample.getName())) {
                    prosecutionHypothesis.addContributor(sample, hpContributors.get(sample.getName()));
                }
                else {
                    prosecutionHypothesis.addNonContributor(sample, 0);
                }
            }

            prosecutionHypothesis.setCandidateDropout(reader.getCandidateDropout());

            _messageBus.send(this, new OutputLocationMessage(reader.getResultLocation()));
            _messageBus.send(this, new DefenseHypothesisMessage(defenseHypothesis));
            _messageBus.send(this, new ProsecutionHypothesisMessage(prosecutionHypothesis));

            final Double freq = reader.getRareAlleleFrequency();
            if (freq != null) {
                _messageBus.send(this, new RareAlleleFrequencyMessage(freq));
            }

            _messageBus.waitIdle(3000);
            LOG.debug("Succesfully loaded search criteria");
            return true;
        }
        catch (final Throwable t) {
            LOG.error("Failed to load search criteria!", t);
            _messageBus.send(this, new ErrorStringMessage("<html>Failed to load search criteria:<br>" + t.getClass().getSimpleName() + (t.getLocalizedMessage() == null ? "" : ": " + t.getLocalizedMessage())));
        }
        finally {
            setApplicationStatus();
        }
        return false;
    }

    private void setApplicationStatus() {
        ApplicationStatus status = ApplicationStatus.READY_FOR_ANALYSIS;
        if (_messageBus.query(DatabaseMessage.class) == null) {
            status = ApplicationStatus.WAIT_DB;
        }
        else {
            if (SmartRankRestrictions.isBatchMode()) {
                status = ApplicationStatus.BATCHMODE_IDLE;
            }
            else {
                final Collection<Sample> crimesceneProfiles = _messageBus.query(CrimeSceneProfilesMessage.class);
                if (crimesceneProfiles == null || getEnabledCount(crimesceneProfiles) == 0) {
                    status = ApplicationStatus.WAIT_CRIMESCENE_PROFILES;
                }
                else {
                    if (_messageBus.query(PopulationStatisticsMessage.class) == null) {
                        status = ApplicationStatus.WAIT_POPULATION_STATISTICS;
                    }
                }
            }
        }
        LOG.debug("Set application status to {}", status);
        _messageBus.send(this, new ApplicationStatusMessage(status));
    }

    public synchronized DefenseHypothesis getDefenseHypothesis() {
        DefenseHypothesis hypothesis = _messageBus.query(DefenseHypothesisMessage.class);
        if (hypothesis == null) {
            hypothesis = new DefenseHypothesis();
            _messageBus.send(this, new DefenseHypothesisMessage(hypothesis));
        }
        return hypothesis;
    }

    public synchronized ProsecutionHypothesis getProsecutionHypothesis() {
        ProsecutionHypothesis hypothesis = _messageBus.query(ProsecutionHypothesisMessage.class);
        if (hypothesis == null) {
            hypothesis = new ProsecutionHypothesis();
            _messageBus.send(this, new ProsecutionHypothesisMessage(hypothesis));
        }
        return hypothesis;
    }

    public int getEnabledCount(final Collection<Sample> profiles) {
        int enabledCount = 0;
        for (final Sample sample : profiles) {
            if (sample.isEnabled()) {
                enabledCount++;
            }
        }
        return enabledCount;
    }

    public void updateEnabledLoci() {
        final OrderMergedList<String> enabledLoci = new OrderMergedList<>();
        final Collection<Sample> replicates = NullUtils.getValue(_messageBus.query(CrimeSceneProfilesMessage.class), new ArrayList<Sample>());
        for (final Sample replicate : replicates) {
            for (final Locus replicateLocus : replicate.getLoci()) {
                enabledLoci.add(replicateLocus.getName());
            }
        }

        // If a locus was present in the sample but not in known profiles, it is ignored
        final Collection<Sample> knowns = NullUtils.getValue(_messageBus.query(KnownProfilesMessage.class), new ArrayList<Sample>());
        for (final Sample sample : knowns) {
            final Iterator<String> iter = enabledLoci.iterator();
            while (iter.hasNext()) {
                final Locus locus = sample.getLocus(iter.next());
                if (locus == null || locus.size() == 0) {
                    iter.remove();
                }
            }
        }

        final PopulationStatistics stats = _messageBus.query(PopulationStatisticsMessage.class);
        if (stats != null) {
            final Collection<String> statsLoci = stats.getLoci();
            final Iterator<String> iter = enabledLoci.iterator();
            while (iter.hasNext()) {
                final String locusName = iter.next();
                if (!statsLoci.contains(locusName)) {
                    iter.remove();
                }
            }
        }

        _messageBus.send(this, new EnabledLociMessage(enabledLoci));
    }

    public static void reset() {
        _me = null;
    }
}
