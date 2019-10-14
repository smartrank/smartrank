package nl.minvenj.nfi.smartrank.io.databases.codis;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import nl.minvenj.nfi.smartrank.analysis.ExcludedProfile;
import nl.minvenj.nfi.smartrank.io.databases.DatabaseValidationEventListener;

@RunWith(Parameterized.class)
public class CodisDatabaseReaderTest {

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
            {"database_codis_generated_44.csv", 44, Arrays.asList("SHA-1/F9031B350107BB10BA8FEBE31BAECC36ECBA13A9", "SHA-1/F0EB5CE841858A93AC84785AD311E0DD986D89C0"), "CODIS",
                new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 44, 0)),
                new ArrayList<>(Arrays.asList("D16S539=44", "D18S51=44", "D22S1045=44", "D2S1338=44", "D5S818=44", "D7S820=44", "D2S441=44", "D12S391=44", "TPOX=44", "TH01=44", "FGA=44", "VWA=44", "D3S1358=44", "SE33=44", "CSF1PO=44", "PENTAE=44", "PENTAD=44", "D1S1656=44", "D8S1179=44", "D13S317=44", "D10S1248=44", "D19S433=44", "D21S11=44"))},
            {"database_codis_generated_10.csv", 10, Arrays.asList("SHA-1/FFD497A7F5566D8B54E7C4E9F384A7AD9463B8AB", "SHA-1/939DA2EA1DC4512BADF2754A93EBA2D0DB37EA19"), "CODIS",
                new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 3, 2, 1, 0, 1, 0)),
                new ArrayList<>(Arrays.asList("D7S820=6", "FGA=10", "D1S1656=1", "TPOX=6", "D19S433=7", "D5S818=8", "D10S1248=2", "PENTA_D=3", "D22S1045=2", "D2S1338=9", "PENTA_E=2", "D21S11=10", "D12S391=2", "D18S51=10", "TH01=10", "SE33=2", "D16S539=10", "D13S317=4", "CSF1PO=6", "D2S441=3", "D3S1358=10", "D8S1179=10"))},
            {"database_codis_generated_10_2fields.csv", 10, Arrays.asList("SHA-1/E8E77A4943694E23BBCDB905238940B1D1C7C065", "SHA-1/B82F642E2EB0BFB8845CC7583DC45A8CEF005C55"), "CODIS",
                new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 3, 2, 1, 0, 1, 0)),
                new ArrayList<>(Arrays.asList("D7S820=6", "FGA=10", "D1S1656=1", "TPOX=6", "D19S433=7", "D5S818=8", "D10S1248=2", "PENTA_D=3", "D22S1045=2", "D2S1338=9", "PENTA_E=2", "D21S11=10", "D12S391=2", "D18S51=10", "TH01=10", "SE33=2", "D16S539=10", "D13S317=4", "CSF1PO=6", "D2S441=3", "D3S1358=10", "D8S1179=10"))},
            {"database_codis_generated_10_3fields.csv", 10, Arrays.asList("SHA-1/3A04A704B49C284ED2566EAE49B21749A855F5A1", "SHA-1/D6C31B042E6D9493EAB2D7D613D1AEFA0E7ED02E"), "CODIS",
                new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 3, 2, 1, 0, 1, 0)),
                new ArrayList<>(Arrays.asList("D7S820=6", "FGA=10", "D1S1656=1", "TPOX=6", "D19S433=7", "D5S818=8", "D10S1248=2", "PENTA_D=3", "D22S1045=2", "D2S1338=9", "PENTA_E=2", "D21S11=10", "D12S391=2", "D18S51=10", "TH01=10", "SE33=2", "D16S539=10", "D13S317=4", "CSF1PO=6", "D2S441=3", "D3S1358=10", "D8S1179=10"))},
            {"database_codis_generated_10_6fields.csv", 10, Arrays.asList("SHA-1/7AEC37D4DBBDE9D7AE7E105FD0F2327258D1DAD1", "SHA-1/7C5A2E1C91E9DE84BB2FCB8E48C5A149F2B9361C"), "CODIS",
                new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 3, 2, 1, 0, 1, 0)),
                new ArrayList<>(Arrays.asList("D7S820=6", "FGA=10", "D1S1656=1", "TPOX=6", "D19S433=7", "D5S818=8", "D10S1248=2", "PENTA_D=3", "D22S1045=2", "D2S1338=9", "PENTA_E=2", "D21S11=10", "D12S391=2", "D18S51=10", "TH01=10", "SE33=2", "D16S539=10", "D13S317=4", "CSF1PO=6", "D2S441=3", "D3S1358=10", "D8S1179=10"))}
        });
    }

    @Parameter(0)
    public String _fileName;

    @Parameter(1)
    public int _recordCount;

    @Parameter(2)
    public List<String> _expectedHashes;

    @Parameter(3)
    public String _expectedFormat;

    @Parameter(4)
    public List<Integer> _expectedSpecimenCountPerNumberOfLoci;

    @Parameter(5)
    public List<String> _expectedSpecimenCountsPerLocus;

    @Test
    public final void testGetRecordCount() throws FileNotFoundException, MalformedURLException, IOException, InterruptedException, URISyntaxException {
        final CodisDatabaseReader reader = getValidatedReader();
        final int actualRecordCount = reader.getRecordCount();
        assertEquals("Record count differs!", _recordCount, actualRecordCount);
    }

    private CodisDatabaseReader getValidatedReader() throws FileNotFoundException, MalformedURLException, IOException, URISyntaxException, InterruptedException {
        final CodisDatabaseReader reader = new CodisDatabaseReader(new File(getClass().getResource(_fileName).toURI()));
        reader.validate(new DatabaseValidationEventListener() {

            @Override
            public void onProgress(final long current, final long max) {
            }

            @Override
            public void onProblem(final String specimen, final String locus, final String message) {
                fail("Unexpected validation problem: Specimen '" + specimen + "', Locus '" + locus + "', " + message);
            }
        });
        return reader;
    }

    @Test
    public final void testValidateTwice() throws FileNotFoundException, MalformedURLException, IOException, URISyntaxException, InterruptedException {
        final CodisDatabaseReader reader = getValidatedReader();
        reader.validate(new DatabaseValidationEventListener() {

            @Override
            public void onProgress(final long current, final long max) {
            }

            @Override
            public void onProblem(final String specimen, final String locus, final String message) {
                fail("Failed second validation! specimen=" + specimen + " locus=" + locus + ": " + message);
            }
        });
        final String contentHash = reader.getContentHash();
        assertTrue("Content hash differs. Expected one of " + _expectedHashes + " but got " + contentHash, _expectedHashes.contains(contentHash));
    }

    @Test
    public final void testGetContentHash() throws FileNotFoundException, MalformedURLException, IOException, URISyntaxException, InterruptedException {
        final CodisDatabaseReader reader = getValidatedReader();
        final String contentHash = reader.getContentHash();
        assertTrue("Content hash differs. Expected one of " + _expectedHashes + " but got " + contentHash, _expectedHashes.contains(contentHash));
    }

    @Test
    public final void testGetFormatName() throws FileNotFoundException, MalformedURLException, IOException, URISyntaxException, InterruptedException {
        final CodisDatabaseReader reader = getValidatedReader();
        final String formatName = reader.getFormatName();
        assertEquals("Format Name differs!", _expectedFormat, formatName);
    }

    @Test
    public final void testGetBadRecordList() throws FileNotFoundException, MalformedURLException, IOException, URISyntaxException, InterruptedException {
        final CodisDatabaseReader reader = getValidatedReader();
        final List<ExcludedProfile> badRecordList = reader.getBadRecordList();
        assertNotNull("Bad Record List is null!", badRecordList);
        assertTrue("Bad Record List is not empty: " + badRecordList, badRecordList.isEmpty());
    }

    @Test
    public final void testGetSpecimenCountPerNumberOfLoci() throws FileNotFoundException, MalformedURLException, IOException, URISyntaxException, InterruptedException {
        final CodisDatabaseReader reader = getValidatedReader();
        final List<Integer> specimenCountPerNumberOfLoci = reader.getSpecimenCountPerNumberOfLoci();
        assertNotNull("specimenCountPerNumberOfLoci unexpectedly null!", specimenCountPerNumberOfLoci);
        assertArrayEquals("specimenCountPerNumberOfLoci differs!", _expectedSpecimenCountPerNumberOfLoci.toArray(), specimenCountPerNumberOfLoci.toArray());
    }

    @Test
    public final void testGetSpecimenCountsPerLocus() throws FileNotFoundException, MalformedURLException, IOException, URISyntaxException, InterruptedException {
        final CodisDatabaseReader reader = getValidatedReader();
        final Map<String, Integer> specimenCountsPerLocus = reader.getSpecimenCountsPerLocus();
        assertNotNull("specimenCountsPerLocus unexpectedly null!", specimenCountsPerLocus);

        for (final String locusName : specimenCountsPerLocus.keySet()) {
            final Integer count = specimenCountsPerLocus.get(locusName);
            assertTrue("Unexpected specimen count " + count + " for locus " + locusName, _expectedSpecimenCountsPerLocus.remove(locusName + "=" + count));
        }

        assertTrue("Expected specimen counts not found: " + _expectedSpecimenCountsPerLocus, _expectedSpecimenCountsPerLocus.isEmpty());
    }
}
