package nl.minvenj.nfi.smartrank.io.searchcriteria.locim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.statistics.StatisticsReader;

@RunWith(Parameterized.class)
public class SmartRankImportFileReaderTest {

    @Parameters(name = "{0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
            {"SmartRankImportFile-sample.xml", //  0, filename
                0.03, //  1, theta
                0.05, //  2, dropin
                Arrays.asList("CEDU0059FR#07-KOEL0126LU#21"), //  3, samples
                Arrays.asList("CEDU0059FR#07"), //  4, knowns
                0.1, //  5, HP candidate dropout
                Arrays.asList("CEDU0059FR#07"), //  6, HP contributors
                Arrays.asList(0.11), //  7, HP contributors dropout
                1, //  8, HP unknowns
                0.12, //  9, HP unknowns dropout
                Arrays.asList("CEDU0059FR#07"), // 10, HD contributors
                Arrays.asList(0.13), // 11, HD contributors dropout
                2, // 12, HD unknowns
                0.14, // 13, HD unknowns dropout
                "2016/01 januari/2016.01.01.001", // 14, result location
                "MASLA", // 15, requested by
                new Date(1471872263000L), // 16, request date and time
                1234, // 17, LR threshold
                0.000239808153477218, // 18, rare allele frequency
                -1, // 19, maximum number of results
                false, // 20, is automatic parameter estimation to be performed
                null, // 21, statistics filename
                null // 22, properties
        },
            {
                "SmartRankImportFile-sample-withproperties.xml", //  0, filename
                0.03, //  1, theta
                0.05, //  2, dropin
                Arrays.asList("CEDU0059FR#07-KOEL0126LU#21"), //  3, samples
                Arrays.asList("CEDU0059FR#07"), //  4, knowns
                0.1, //  5, HP candidate dropout
                Arrays.asList("CEDU0059FR#07"), //  6, HP contributors
                Arrays.asList(0.11), //  7, HP contributors dropout
                1, //  8, HP unknowns
                0.12, //  9, HP unknowns dropout
                Arrays.asList("CEDU0059FR#07"), // 10, HD contributors
                Arrays.asList(0.13), // 11, HD contributors dropout
                2, // 12, HD unknowns
                0.14, // 13, HD unknowns dropout
                "2016/01 januari/2016.01.01.001", // 14, result location
                "MASLA", // 15, requested by
                new Date(1471872263000L), // 16, request date and time
                1234, // 17, LR threshold
                0.000239808153477218, // 18, rare allele frequency
                -1, // 19, maximum number of results
                false, // 20, is automatic parameter estimation to be performed
                null, // 21, statistics filename
                Arrays.asList("property1", "value1", "property2", "value2") // 22, properties
            },
            {"SmartRankImportFile-sample-nodropin.xml", //  0, filename
                0.03, //  1, theta
                0, //  2, dropin
                Arrays.asList("CEDU0059FR#07-KOEL0126LU#21"), //  3, samples
                Arrays.asList("CEDU0059FR#07"), //  4, knowns
                0.1, //  5, HP candidate dropout
                Arrays.asList("CEDU0059FR#07"), //  6, HP contributors
                Arrays.asList(0.11), //  7, HP contributors dropout
                1, //  8, HP unknowns
                0.12, //  9, HP unknowns dropout
                Arrays.asList("CEDU0059FR#07"), // 10, HD contributors
                Arrays.asList(0.13), // 11, HD contributors dropout
                2, // 12, HD unknowns
                0.14, // 13, HD unknowns dropout
                "2016/01 januari/2016.01.01.001", // 14, result location
                "MASLA", // 15, requested by
                new Date(1471872263000L), // 16, request date and time
                1234, // 17, LR threshold
                0.000239808153477218, // 18, rare allele frequency
                -1, // 19, maximum number of results
                false, // 20, is automatic parameter estimation to be performed
                null, // 21, statistics filename
                null // 22, properties
        },
            {"SmartRankImportFile-sample-nodatetime.xml", //  0, filename
                0.03, //  1, theta
                0.05, //  2, dropin
                Arrays.asList("CEDU0059FR#07-KOEL0126LU#21"), //  3, samples
                Arrays.asList("CEDU0059FR#07"), //  4, knowns
                0.1, //  5, HP candidate dropout
                Arrays.asList("CEDU0059FR#07"), //  6, HP contributors
                Arrays.asList(0.11), //  7, HP contributors dropout
                1, //  8, HP unknowns
                0.12, //  9, HP unknowns dropout
                Arrays.asList("CEDU0059FR#07"), // 10, HD contributors
                Arrays.asList(0.13), // 11, HD contributors dropout
                2, // 12, HD unknowns
                0.14, // 13, HD unknowns dropout
                "2016/01 januari/2016.01.01.001", // 14, result location
                "MASLA", // 15, requested by
                null, // 16, request date and time
                1234, // 17, LR threshold
                0.000239808153477218, // 18, rare allele frequency
                -1, // 19, maximum number of results
                false, // 20, is automatic parameter estimation to be performed
                null, // 21, statistics filename,
                null // 22, properties
        },
            {"SmartRankImportFile-sample-malformatteddatetime.xml", //  0, filename
                0.03, //  1, theta
                0.05, //  2, dropin
                Arrays.asList("CEDU0059FR#07-KOEL0126LU#21"), //  3, samples
                Arrays.asList("CEDU0059FR#07"), //  4, knowns
                0.1, //  5, HP candidate dropout
                Arrays.asList("CEDU0059FR#07"), //  6, HP contributors
                Arrays.asList(0.11), //  7, HP contributors dropout
                1, //  8, HP unknowns
                0.12, //  9, HP unknowns dropout
                Arrays.asList("CEDU0059FR#07"), // 10, HD contributors
                Arrays.asList(0.13), // 11, HD contributors dropout
                2, // 12, HD unknowns
                0.14, // 13, HD unknowns dropout
                "2016/01 januari/2016.01.01.001", // 14, result location
                "MASLA", // 15, requested by
                null, // 16, request date and time
                1234, // 17, LR threshold
                0.000239808153477218, // 18, rare allele frequency
                -1, // 19, maximum number of results
                false, // 20, is automatic parameter estimation to be performed
                null, // 21, statistics filename
                null // 22, properties
        }
            ,
            {"SmartRankImportFile-sample-statistics.xml", //  0, filename
                0.03, //  1, theta
                0.05, //  2, dropin
                Arrays.asList("CEDU0059FR#07-KOEL0126LU#21"), //  3, samples
                Arrays.asList("CEDU0059FR#07"), //  4, knowns
                0.1, //  5, HP candidate dropout
                Arrays.asList("CEDU0059FR#07"), //  6, HP contributors
                Arrays.asList(0.11), //  7, HP contributors dropout
                1, //  8, HP unknowns
                0.12, //  9, HP unknowns dropout
                Arrays.asList("CEDU0059FR#07"), // 10, HD contributors
                Arrays.asList(0.13), // 11, HD contributors dropout
                2, // 12, HD unknowns
                0.14, // 13, HD unknowns dropout
                "2016/01 januari/2016.01.01.001", // 14, result location
                "MASLA", // 15, requested by
                new Date(1471872263000L), // 16, request date and time
                1234, // 17, LR threshold
                0.000239808153477218, // 18, rare allele frequency
                -1, // 19, maximum number of results
                false, // 20, is automatic parameter estimation to be performed
                "DummyStatistics.csv", // 21, statistics filename
                null // 22, properties
            }
            ,
            {"SmartRankImportFile-sample-automatic.xml", //  0, filename
                0.03, //  1, theta
                0.05, //  2, dropin
                Arrays.asList("CEDU0059FR#07-KOEL0126LU#21"), //  3, samples
                Arrays.asList("CEDU0059FR#07"), //  4, knowns
                0, //  5, HP candidate dropout
                Arrays.asList("CEDU0059FR#07"), //  6, HP contributors
                Arrays.asList(0.0), //  7, HP contributors dropout
                1, //  8, HP unknowns
                0, //  9, HP unknowns dropout
                Arrays.asList("CEDU0059FR#07"), // 10, HD contributors
                Arrays.asList(0.0), // 11, HD contributors dropout
                2, // 12, HD unknowns
                0, // 13, HD unknowns dropout
                "2016/01 januari/2016.01.01.001", // 14, result location
                "MASLA", // 15, requested by
                new Date(1471872263000L), // 16, request date and time
                1234, // 17, LR threshold
                0.000239808153477218, // 18, rare allele frequency
                -1, // 19, maximum number of results
                true, // 20, is automatic parameter estimation to be performed
                null, // 21, statistics filename
                null // 22, properties
            }
        });
    }

    @Parameter(0)
    public String _fileName;

    @Parameter(1)
    public double _theta;

    @Parameter(2)
    public double _dropin;

    @Parameter(3)
    public List<String> _crimesceneSamples;

    @Parameter(4)
    public List<String> _knownProfiles;

    @Parameter(5)
    public double _candidateDropout;

    @Parameter(6)
    public List<String> _hpContributors;

    @Parameter(7)
    public List<String> _hpContributorDropouts;

    @Parameter(8)
    public int _hpUnknowns;

    @Parameter(9)
    public double _hpUnknownsDropout;

    @Parameter(10)
    public List<String> _hdContributors;

    @Parameter(11)
    public List<String> _hdContributorDropouts;

    @Parameter(12)
    public int _hdUnknowns;

    @Parameter(13)
    public double _hdUnknownsDropout;

    @Parameter(14)
    public String _resultLocation;

    @Parameter(15)
    public String _requestedBy;

    @Parameter(16)
    public Date _requestDateTime;

    @Parameter(17)
    public Integer _lrThreshold;

    @Parameter(18)
    public Double _rareAlleleFrequency;

    @Parameter(19)
    public int _maximumNumberOfResults;

    @Parameter(20)
    public boolean _isAutomaticParameterEstimationToBePerformed;

    @Parameter(21)
    public String _populationStatisticsFilename;

    @Parameter(22)
    public List<String> _properties;

    private SmartRankImportFileReader _reader;
    private PopulationStatistics _populationStatistics;



    @Before
    public void setUp() throws Exception {
        if (getClass().getResource(_fileName) == null) {
            fail("Testfile " + _fileName + " could not be found!");
        }
        System.out.println("Reading from " + getClass().getResource(_fileName).toURI());
        _reader = new SmartRankImportFileReader(new File(getClass().getResource(_fileName).toURI()));

        if (_populationStatisticsFilename != null) {
            _populationStatistics = new StatisticsReader(new File(getClass().getResource(_populationStatisticsFilename).toURI())).getStatistics();
        }
    }

    @Test
    public final void testGetCrimesceneSamples() {
        final List<Sample> crimesceneSamples = _reader.getCrimesceneSamples();
        assertNotNull(crimesceneSamples);
        assertEquals(_crimesceneSamples.size(), crimesceneSamples.size());
        for (final Sample sample : crimesceneSamples) {
            assertTrue("Unexpected sample '" + sample.getName() + "'", _crimesceneSamples.contains(sample.getName()));
        }
    }

    @Test
    public final void testGetKnownProfiles() {
        final List<Sample> knownProfiles = _reader.getKnownProfiles();
        assertNotNull(knownProfiles);
        assertEquals(_knownProfiles.size(), knownProfiles.size());
        for (final Sample sample : knownProfiles) {
            assertTrue("Unexpected known profile '" + sample.getName() + "'", _knownProfiles.contains(sample.getName()));
        }
    }

    @Test
    public final void testGetHpContributors() {
        final Map<String, Double> hpContributors = _reader.getHpContributors();
        assertEquals(_hpContributors.size(), hpContributors.size());
        assertEquals(_hpContributorDropouts.size(), hpContributors.size());

        for (int idx = 0; idx < _hpContributors.size(); idx++) {
            final Double dropout = hpContributors.get(_hpContributors.get(idx));
            assertNotNull(dropout);
            assertEquals(_hpContributorDropouts.get(idx), dropout);
        }
    }

    @Test
    public final void testGetHpUnknowns() {
        assertEquals(_hpUnknowns, _reader.getHpUnknowns());
    }

    @Test
    public final void testGetHpUnknownDropout() {
        assertEquals(_hpUnknownsDropout, _reader.getHpUnknownDropout(), 0.0001);
    }

    @Test
    public final void testGetHdContributors() {
        final Map<String, Double> hdContributors = _reader.getHdContributors();
        assertEquals(_hdContributors.size(), hdContributors.size());
        assertEquals(_hdContributorDropouts.size(), hdContributors.size());

        for (int idx = 0; idx < _hdContributors.size(); idx++) {
            final Double dropout = hdContributors.get(_hdContributors.get(idx));
            assertNotNull(dropout);
            assertEquals(_hdContributorDropouts.get(idx), dropout);
        }
    }

    @Test
    public final void testGetHdUnknowns() {
        assertEquals(_hdUnknowns, _reader.getHdUnknowns());
    }

    @Test
    public final void testGetHdUnknownDropout() {
        assertEquals(_hdUnknownsDropout, _reader.getHdUnknownDropout(), 0.0001);
    }

    @Test
    public final void testGetResultLocation() {
        assertEquals(_resultLocation, _reader.getResultLocation());
    }

    @Test
    public final void testGetCandidateDropout() {
        assertEquals(_candidateDropout, _reader.getCandidateDropout(), 0.001);
    }

    @Test
    public final void testGetTheta() {
        assertEquals(_theta, _reader.getTheta(), 0.001);
    }

    @Test
    public final void testGetDropin() {
        assertEquals(_dropin, _reader.getDropin(), 0.001);
    }

    @Test
    public void testGetRequester() {
        assertEquals(_requestedBy, _reader.getRequester());
    }

    @Test
    public void testGetRequestDateTime() {
        if (_requestDateTime != null) {
            assertEquals(_requestDateTime, _reader.getRequestDateTime());
        }
    }

    @Test
    public void testGetLRThreshold() {
        assertEquals(_lrThreshold, _reader.getLRThreshold());
    }

    @Test
    public void testGetRareAlleleFrequency() {
        assertEquals(_rareAlleleFrequency, _reader.getRareAlleleFrequency());
    }

    @Test
    public void testGetMaximumNumberOfResults() {
        assertEquals(_maximumNumberOfResults, _reader.getMaximumNumberOfResults());
    }

    @Test
    public void testIsAutomaticParameterEstimationToBePerformed() {
        assertEquals(_isAutomaticParameterEstimationToBePerformed, _reader.isAutomaticParameterEstimationToBePerformed());
    }

    @Test
    public void testGetPopulationStatistics() {
        if (_populationStatistics == null) {
            assertNull(_reader.getPopulationStatistics());
        }
        else {
            compareStatistics(_populationStatistics, _reader.getPopulationStatistics());
            compareStatistics(_reader.getPopulationStatistics(), _populationStatistics);
        }
    }

    @Test
    public void testProperties() {
        final Properties props = _reader.getProperties();
        assertNotNull(props);
        if (_properties != null) {
            int idx = 0;
            while (idx < _properties.size()) {
                final String key = _properties.get(idx);
                final String value = _properties.get(idx + 1);
                assertTrue("Property " + key + " with value " + value + " not found in " + props, props.remove(key, value));
                idx += 2;
            }
            assertTrue("Unexpected additional properties: " + props, props.isEmpty());
        }
        else {
            assertEquals(new Properties(), props);
        }
    }

    private void compareStatistics(final PopulationStatistics stats, final PopulationStatistics otherStats) {
        for (final String locusName : stats.getLoci()) {
            for (final String allele : stats.getAlleles(locusName)) {
                assertEquals("Stats differ at locus " + locusName + "." + allele, stats.getProbability(locusName, allele), otherStats.getProbability(locusName, allele));
            }
        }
    }
}
