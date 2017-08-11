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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
import nl.minvenj.nfi.smartrank.domain.LikelihoodRatio;
import nl.minvenj.nfi.smartrank.io.WritableFileSource;
import nl.minvenj.nfi.smartrank.messages.commands.StartAnalysisCommand;
import nl.minvenj.nfi.smartrank.messages.commands.WritableFileSourceMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseFileMessage;
import nl.minvenj.nfi.smartrank.messages.data.LRThresholdMessage;
import nl.minvenj.nfi.smartrank.messages.data.LoadSearchCriteriaMessage;
import nl.minvenj.nfi.smartrank.messages.data.PopulationStatisticsFileMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.messages.status.PercentReadyMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchAbortedMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchCompletedMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.raven.numberformat.NumberUtils;

@RunWith(Parameterized.class)
public class BatchValidationTest {

    private static final Logger LOG = LoggerFactory.getLogger(BatchValidationTest.class);

    @Parameters(name = "DB {0}, Search Criteria {1}, Expected Results {2}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
            {"database_codis_generated_100.csv", "FOEC0000BE#08-GBCX0000LU#09-SearchCriteria.xml", "FOEC0000BE#08-GBCX0000LU#09-expectedresults.txt"},
            {"SMARTRANK-42-database.csv", "SMARTRANK-42-SearchCriteria.xml", "SMARTRANK-42-expectedresults.txt"},
            {"SMARTRANK-210-database.csv", "SMARTRANK-210-SearchCriteria.xml", "SMARTRANK-210-expectedresults.txt"},
            {"SMARTRANK-261-database.csv", "SMARTRANK-261-SearchCriteria.xml", "SMARTRANK-261-expectedresults.txt"}
        });
    }

    @Parameter(0)
    public String _connectString;

    @Parameter(1)
    public String _searchCriteriaFilename;

    @Parameter(2)
    public String _resultsFileName;

    private HashMap<String, Double> _expectedResults;

    private Map<String, String> _expectedLoci;

    private String _message;

    private SearchResults _results;

    private SmartRankManager _smartRankManager;

    private MessageBus _messageBus;

    private int _percent;

    private AtomicBoolean _analysisCanBeStarted;

    @Before
    public void setUp() throws Exception {

        System.setProperty("qDesignationShutdown", "true");

        _percent = 0;
        _expectedResults = new HashMap<>();
        _expectedLoci = new HashMap<>();
        _analysisCanBeStarted = new AtomicBoolean(true);

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
                    if (parts.length > 2) {
                        _expectedLoci.put(parts[0], parts[2]);
                    }
                }
            }
        }
        catch (final NumberFormatException nfe) {
            fail("File containing expected results '" + _resultsFileName + "' is not valid: NumberFormatException " + nfe.getMessage());
        }

        // Call the reset methods to avoid some JUnit-specific misery associated with objects being reused.
        MessageBus.getInstance().waitIdle(5000);
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
                _messageBus.send(this, new LRThresholdMessage(1000));
                _messageBus.send(this, new PopulationStatisticsFileMessage(new File(getClass().getResource("str_base_validation_file.xml").toURI())));
                _messageBus.send(this, new LoadSearchCriteriaMessage(Arrays.asList(new File(getClass().getResource(_searchCriteriaFilename).toURI()))));
                break;
            case READY_FOR_ANALYSIS:
                if (_analysisCanBeStarted.getAndSet(false)) {
                    LOG.info("Initiating search");
                    _messageBus.send(this, new StartAnalysisCommand());
                }
                break;
            default:
                LOG.info("Status = {}", status);
        }
    }

    @RavenMessageHandler(ErrorStringMessage.class)
    private void onErrorMessage(final String message) {
        _message = message;
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

            String expectedLoci = _expectedLoci.get(lr.getProfile().getName());
            if (expectedLoci != null) {
                expectedLoci = expectedLoci.replaceAll("\\,", " ");
                for (final String locus : lr.getLoci()) {
                    assertTrue("Unexpected result for locus '" + locus + "' in results " + lr.getLoci(), expectedLoci.contains(locus));
                    expectedLoci = expectedLoci.replaceFirst(locus, "");
                }
                expectedLoci = expectedLoci.trim();
                assertTrue("Expected results not received for locus/loci: [" + expectedLoci.replaceAll(" ", ", ") + "] in results " + lr.getLoci(), expectedLoci.isEmpty());
            }
        }
        assertTrue("Expected results were not found for profile(s) " + _expectedResults.keySet(), _expectedResults.size() == 0);
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
