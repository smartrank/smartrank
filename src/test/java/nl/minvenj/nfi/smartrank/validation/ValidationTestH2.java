package nl.minvenj.nfi.smartrank.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
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
import nl.minvenj.nfi.smartrank.domain.Contributor;
import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.LikelihoodRatio;
import nl.minvenj.nfi.smartrank.domain.ProblemLocation;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.io.CSVReader;
import nl.minvenj.nfi.smartrank.io.WritableFileSource;
import nl.minvenj.nfi.smartrank.io.databases.jdbc.drivers.H2DriverWrapper;
import nl.minvenj.nfi.smartrank.messages.commands.StartAnalysisCommand;
import nl.minvenj.nfi.smartrank.messages.commands.WritableFileSourceMessage;
import nl.minvenj.nfi.smartrank.messages.data.AddCrimeSceneFilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.AddKnownFilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseConnectionMessage;
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
public class ValidationTestH2 {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationTestH2.class);

    private static final class Donor {
        private final String _file;
        private final double _dropout;

        public Donor(final String file, final double dropout) {
            _file = file;
            _dropout = dropout;
        }

        public String getFile() {
            return _file;
        }

        public double getDropout() {
            return _dropout;
        }

        @Override
        public String toString() {
            return _file + " (" + _dropout + ")";
        }
    }

    @Parameters(name = "DB {0}, Sample {1}, Knowns {2}, Stats {3}, Dropin {5}, Theta {6}, Hp Candidate ({7}) + {8} unknowns ({9}), Hd {10} unknowns ({11})")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
            {"database_codis_generated_100.csv", "FOEC0000BE#08-GBCX0000LU#09.csv", null, "str_base_validation_file.xml", 0.0003, 0.05, 0.01, 0.03, 1, 0.03, 2, 0.03, "FOEC0000BE#08-GBCX0000LU#09-expectedresults.txt", 0},
            {"SMARTRANK-27-database.csv", "SMARTRANK-27-sample.csv", null, "str_base_validation_file.xml", 0.0001, 0.05, 0.01, 0.03, 1, 0.03, 2, 0.03, "SMARTRANK-27-expectedresults.txt", 0},
            {"SMARTRANK-210-database.csv", "SMARTRANK-210-sample.csv", new Donor[]{new Donor("SMARTRANK-210-known.csv", 0)}, "str_base_validation_file.xml", 0.0001, 0.05, 0.01, 0.03, 1, 0, 2, 0, "SMARTRANK-210-expectedresults.txt", 0},
            {"SMARTRANK-234-database.csv", "SMARTRANK-234-sample.csv", null, "str_base_validation_file.xml", 0.0001, 0.05, 0.01, 0.03, 1, 0, 2, 0, "SMARTRANK-234-expectedresults.txt", 0},
        });
    }

    @Parameter(0)
    public String _connectString;

    @Parameter(1)
    public String _crimeSample;

    @Parameter(2)
    public Donor[] _knownDonors;

    @Parameter(3)
    public String _stats;

    @Parameter(4)
    public double _rareFreq;

    @Parameter(5)
    public double _dropin;

    @Parameter(6)
    public double _theta;

    @Parameter(7)
    public double _hpCandidatePd;

    @Parameter(8)
    public int _hpUnknownCount;

    @Parameter(9)
    public double _hpUnknownPd;

    @Parameter(10)
    public int _hdUnknownCount;

    @Parameter(11)
    public double _hdUnknownPd;

    @Parameter(12)
    public String _resultsFileName;

    @Parameter(13)
    public Integer _problemCount;

    private HashMap<String, Double> _expectedResults;

    private String _message;

    private SearchResults _results;

    private SmartRankManager _smartRankManager;

    private MessageBus _messageBus;

    private int _percent;

    private DatabaseConfiguration _databaseConfiguration;

    @Before
    public void setUp() throws Exception {
        _percent = 0;
        _expectedResults = new HashMap<>();

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

        System.setProperty("smartrankProperties", new File(getClass().getResource("h2/SmartRank.Properties").toURI()).getAbsolutePath());
        System.setProperty("smartrankRestrictions", new File(getClass().getResource("h2/SmartRankRestrictions.properties").toURI()).getAbsolutePath());

        _databaseConfiguration = new DatabaseConfiguration(new H2DriverWrapper(), "mem:test", "", SmartRankGUISettings.getDatabaseUsername(), SmartRankGUISettings.getDatabasePassword(), SmartRankGUISettings.getDatabaseSpecimenKeysQuery(), SmartRankGUISettings.getDatabaseSpecimenQuery(), SmartRankGUISettings.getDatabaseRevisionQuery());
        _databaseConfiguration.setSingleRowQuery(true);

        final Connection connection = DriverManager.getConnection("jdbc:h2:mem:test", SmartRankGUISettings.getDatabaseUsername(), SmartRankGUISettings.getDatabasePassword());
        final CSVReader csvReader = new CSVReader(getClass().getResourceAsStream(_connectString), true);
        final String[] fieldNames = csvReader.readFields();

        // Compose the creation statement
        final StringBuilder createTableSb = new StringBuilder("CREATE TABLE IF NOT EXISTS specimens ( id INT NOT NULL");
        final StringBuilder insertRecordSb = new StringBuilder("INSERT INTO specimens VALUES ( ?");

        // Add fields to create statement for table and insert statement for record
        for (final String field : fieldNames) {
            createTableSb.append(", ").append(field).append(" VARCHAR(50)");
            insertRecordSb.append(", ?");
        }

        // Finalize statements
        createTableSb.append(");");
        insertRecordSb.append(");");

        connection.createStatement().execute("DROP TABLE specimens IF EXISTS;");
        connection.createStatement().execute(createTableSb.toString());
        final PreparedStatement insertPs = connection.prepareStatement(insertRecordSb.toString());

        String[] fields;
        int currentId = 0;
        while ((fields = csvReader.readFields()) != null) {
            if (fields.length != fieldNames.length) {
                LOG.info("Record {} skipped: {} fields, but {} values!", fields[0], fieldNames.length, fields.length);
            }
            else {
                insertPs.setInt(1, currentId++);
                for (int idx = 0; idx < fields.length; idx++) {
                    final String field = fields[idx];
                    insertPs.setString(idx + 2, field);
                }
                insertPs.execute();
            }
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
                _messageBus.send(this, new DatabaseConnectionMessage(_databaseConfiguration));
                break;
            case WAIT_CRIMESCENE_PROFILES:
                LOG.info("Loading crimescene profiles");
                _messageBus.send(this, new AddCrimeSceneFilesMessage(new File(getClass().getResource(_crimeSample).toURI())));
                break;
            case WAIT_POPULATION_STATISTICS:
                if (_knownDonors != null && _knownDonors.length > 0) {
                    LOG.info("Loading known donors: {}", (Object[]) _knownDonors);
                    final ArrayList<File> filesList = new ArrayList<>();
                    for (final Donor donor : _knownDonors) {
                        filesList.add(new File(getClass().getResource(donor.getFile()).toURI()));
                    }
                    _messageBus.send(this, new AddKnownFilesMessage(filesList));
                }
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
                    if (_knownDonors != null && _knownDonors.length > 0) {
                        for (final Contributor contributor : def.getContributors()) {
                            for (final Donor donor : _knownDonors) {
                                if (contributor.getSample().getSourceFile().endsWith(donor.getFile())) {
                                    contributor.setDropoutProbability(donor.getDropout());
                                }
                            }
                        }
                    }
                    final ProsecutionHypothesis pro = _messageBus.query(ProsecutionHypothesisMessage.class);
                    pro.setUnknownCount(_hpUnknownCount);
                    pro.setUnknownDropoutProbability(_hpUnknownPd);
                    pro.setDropInProbability(_dropin);
                    pro.setThetaCorrection(_theta);
                    if (_knownDonors != null && _knownDonors.length > 0) {
                        for (final Contributor contributor : pro.getContributors()) {
                            for (final Donor donor : _knownDonors) {
                                if (contributor.getSample().getSourceFile().endsWith(donor.getFile())) {
                                    contributor.setDropoutProbability(donor.getDropout());
                                }
                            }
                        }
                    }
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
                    fail(String.format("Unexpected failure at specimen %s, locus %s: %s", problemLocation.getSpecimen(), problemLocation.getLocus(), problemLocation.getDescription()));
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
