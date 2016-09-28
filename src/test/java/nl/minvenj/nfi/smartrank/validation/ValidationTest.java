package nl.minvenj.nfi.smartrank.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.SmartRankManager;
import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.LikelihoodRatio;
import nl.minvenj.nfi.smartrank.domain.ProblemLocation;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.io.WritableFileSource;
import nl.minvenj.nfi.smartrank.messages.commands.StartAnalysisCommand;
import nl.minvenj.nfi.smartrank.messages.commands.WritableFileSourceMessage;
import nl.minvenj.nfi.smartrank.messages.data.AddCrimeSceneFilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseFileMessage;
import nl.minvenj.nfi.smartrank.messages.data.DefenseHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.LRThresholdMessage;
import nl.minvenj.nfi.smartrank.messages.data.PopulationStatisticsFileMessage;
import nl.minvenj.nfi.smartrank.messages.data.ProsecutionHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.RareAlleleFrequencyMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.messages.status.DatabaseFormatProblemMessage;
import nl.minvenj.nfi.smartrank.messages.status.DetailStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.PercentReadyMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchAbortedMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchCompletedMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.raven.numberformat.NumberUtils;

@RunWith(Parameterized.class)
public class ValidationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationTest.class);

    @Parameters(name = "DB {0}, Sample {1}, Stats {2}, Dropin {4}, Theta {5}, Hp Candidate ({6}) + {7} unknowns ({8}), Hd {9} unknowns ({10})")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
            {"database_codis_generated_100.csv", "FOEC0000BE#08-GBCX0000LU#09.csv", "str_base_validation_file.xml", 0.0003, 0.05, 0.01, 0.03, 1, 0.03, 2, 0.03, "FOEC0000BE#08-GBCX0000LU#09-expectedresults.txt", 0},
            {"SMARTRANK-27-database.csv", "SMARTRANK-27-sample.csv", "str_base_validation_file.xml", 0.0001, 0.05, 0.01, 0.03, 1, 0.03, 2, 0.03, "SMARTRANK-27-expectedresults.txt", 0},
            {"SMARTRANK-42-database.csv", "SMARTRANK-42-sample.csv", "str_base_validation_file.xml", 0.0001, 0.05, 0.01, 0.03, 1, 0.03, 2, 0.03, "SMARTRANK-42-expectedresults.txt", 2},
            {"SMARTRANK-49-database.csv", "SMARTRANK-49-sample.csv", "str_base_validation_file.xml", 0.0001, 0.05, 0.01, 0.03, 1, 0.03, 2, 0.03, "SMARTRANK-49-expectedresults.txt", 4}
        });
    }

    @Parameter(0)
    public String _connectString;

    @Parameter(1)
    public String _crimeSample;

    @Parameter(2)
    public String _stats;

    @Parameter(3)
    public double _rareFreq;

    @Parameter(4)
    public double _dropin;

    @Parameter(5)
    public double _theta;

    @Parameter(6)
    public double _hpCandidatePd;

    @Parameter(7)
    public int _hpUnknownCount;

    @Parameter(8)
    public double _hpUnknownPd;

    @Parameter(9)
    public int _hdUnknownCount;

    @Parameter(10)
    public double _hdUnknownPd;

    @Parameter(11)
    public String _resultsFileName;

    @Parameter(12)
    public Integer _problemCount;

    private HashMap<String, Double> _expectedResults;

    private String _message;

    private SearchResults _results;

    private SmartRankManager _smartRankManager;

    private MessageBus _messageBus;

    private int _percent;

    @Before
    public void setUp() throws Exception {
        _percent = 0;
        _expectedResults = new HashMap<String, Double>();

        final InputStream inputStream = getClass().getResourceAsStream(_resultsFileName);
        if (inputStream == null) {
            fail("Results file not found: " + _resultsFileName);
        }
        try (BufferedReader resultsReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = resultsReader.readLine()) != null) {
                final String[] parts = line.split(" ");
                if (!parts[0].startsWith("#")) {
                    _expectedResults.put(parts[0], Double.parseDouble(parts[1]));
                }
            }
        }
        catch (final NumberFormatException nfe) {
            fail("File containing expected results '" + _resultsFileName + "' is not valid: NumberFormatException " + nfe.getMessage());
        }

        // Call the reset methods to avoid some JUnit-specific misery associated with objects being reused.
        MessageBus.reset();
        SmartRankManager.reset();

        // Create a new manager to allow this object to register itself on the message bus
        _smartRankManager = SmartRankManager.getInstance();
        assertNotNull(_smartRankManager);

        // Get the message bus for communicating with the rest of the environment
        _messageBus = MessageBus.getInstance();
        assertNotNull("MessageBus is null!", _messageBus);

        // Register ourselves as subscriber to receive messages from the rest of the environment
        _messageBus.registerSubscriber(this);

        _messageBus.send(this, new WritableFileSourceMessage(new WritableFileSource() {
            @Override
            public String getWritableFile(final String fileName) {
                return fileName;
            }

        }));
        _messageBus.send(this, new LRThresholdMessage(1000));
    }

    @RavenMessageHandler(ApplicationStatusMessage.class)
    private void onApplicationStateChange(final ApplicationStatus status) throws Exception {
        switch (status) {
            case WAIT_DB:
                LOG.info("Loading database");
                _messageBus.send(this, new DatabaseFileMessage(new File(getClass().getResource(_connectString).toURI())));
                break;
            case WAIT_CRIMESCENE_PROFILES:
                LOG.info("Loading crimescene profiles");
                _messageBus.send(this, new AddCrimeSceneFilesMessage(new File(getClass().getResource(_crimeSample).toURI())));
                break;
            case WAIT_POPULATION_STATISTICS:
                LOG.info("Loading statistics");
                _messageBus.send(this, new PopulationStatisticsFileMessage(new File(getClass().getResource(_stats).toURI())));
                _messageBus.send(this, new RareAlleleFrequencyMessage(_rareFreq));
                break;
            case READY_FOR_ANALYSIS:
                if (_results == null) {
                    final DefenseHypothesis def = _messageBus.query(DefenseHypothesisMessage.class);
                    def.setDropInProbability(_dropin);
                    def.setThetaCorrection(_theta);
                    def.setUnknownCount(_hdUnknownCount);
                    def.setUnknownDropoutProbability(_hdUnknownPd);

                    final ProsecutionHypothesis pro = _messageBus.query(ProsecutionHypothesisMessage.class);
                    pro.setUnknownCount(_hpUnknownCount);
                    pro.setUnknownDropoutProbability(_hpUnknownPd);
                    pro.setDropInProbability(_dropin);
                    pro.setThetaCorrection(_theta);
                    Thread.sleep(1000);
                    LOG.info("Initiating search");
                    _messageBus.send(this, new StartAnalysisCommand());
                }
                break;
            default:
                LOG.info("Status = {}", status);
        }
    }

    private void checkDatabaseProblemCount() {
        assertEquals("Not all problems were reported! (" + _problemCount + " left unreported)", 0, _problemCount.intValue());
    }

    @RavenMessageHandler(ErrorStringMessage.class)
    private void onErrorMessage(final String message) {
        _message = message;
    }

    @RavenMessageHandler(DatabaseFormatProblemMessage.class)
    public final void onDatabaseProblem(final List<ProblemLocation> problemLocations) {
        synchronized (_problemCount) {
            for (final ProblemLocation problemLocation : problemLocations) {
                if (_problemCount == 0) {
                    fail(String.format("Unexpected failure at record %d, specimen %s, locus %s: %s", problemLocation.getLocation(), problemLocation.getSpecimen(), problemLocation.getLocus(), problemLocation.getDescription()));
                }
                _problemCount--;
            }
        }
    }

    @Test(timeout = 120000)
    public final void compareResults() throws InterruptedException {
        LOG.info("Waiting for results");
        while (_message == null && _results == null) {
            Thread.sleep(100);
        }
        if (_message != null) {
            fail(_message);
        }
        if (!_results.isSucceeded()) {
            fail(_results.getFailureReason().getMessage());
        }

        final Collection<LikelihoodRatio> positiveLRs = _results.getPositiveLRs();
        assertEquals("Expected " + _expectedResults.size() + " positive LRs but got " + positiveLRs.size(), _expectedResults.size(), positiveLRs.size());
        for (final LikelihoodRatio lr : positiveLRs) {
            final Double lrValue = _expectedResults.remove(lr.getProfile().getName());
            assertNotNull("Found an unexpected result for sample " + lr.getProfile().getName() + ": " + lr.getOverallRatio(), lrValue);
            assertEquals("Result for " + lr.getProfile().getName() + "(" + NumberUtils.format(4, lr.getOverallRatio().getRatio()) + ") differs from expected value (" + NumberUtils.format(4, lrValue) + ")!", NumberUtils.format(4, lrValue), NumberUtils.format(4, lr.getOverallRatio().getRatio()));
        }
        assertTrue("Expected results were not found for profile(s) " + _expectedResults.keySet(), _expectedResults.size() == 0);
        checkDatabaseProblemCount();
    }

    @RavenMessageHandler(DetailStringMessage.class)
    public void onDetail(final String detail) {
        LOG.info(detail);
    }

    @RavenMessageHandler(PercentReadyMessage.class)
    public void onProgress(final int percent) {
        if (percent != _percent) {
            System.out.print(percent + "%\r");
            _percent = percent;
        }
    }

    @RavenMessageHandler({SearchCompletedMessage.class, SearchAbortedMessage.class})
    private void onSearchCompleted(final SearchResults results) {
        LOG.info("Search Completed: Success = " + results.isSucceeded());
        _results = results;
    }
}
