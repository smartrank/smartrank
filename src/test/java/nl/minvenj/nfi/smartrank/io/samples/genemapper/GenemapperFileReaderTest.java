package nl.minvenj.nfi.smartrank.io.samples.genemapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;

public class GenemapperFileReaderTest {

    private static final String GENEMAPPER_FILE_WITHOUT_HEIGHTS = "GeneMapperExample.txt";
    private static final String GENEMAPPER_FILE_WITH_HEIGHTS = "GeneMapperExampleWithHeights.txt";
    private static final String GENEMAPPER_FILE_WITH_HEIGHTS_AND_OFFLADDER = "GeneMapperExampleWithHeightsAndOL.txt";
    private static final String GENEMAPPER_FILE_DIFFERENT_CASE_NUMBERS = "GeneMapperExampleDifferentCaseNumbers.txt";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testGenemapperFileReader() {
        final GenemapperFileReader reader = new GenemapperFileReader(getTestFile(GENEMAPPER_FILE_WITHOUT_HEIGHTS));
        assertEquals(GENEMAPPER_FILE_WITHOUT_HEIGHTS, reader.getFile().getName());
    }

    @Test
    public final void testGetFileHash() throws IOException {
        final GenemapperFileReader reader = new GenemapperFileReader(getTestFile(GENEMAPPER_FILE_WITHOUT_HEIGHTS));
        assertEquals("SHA-1/5878581DE97F38A3BA06B87420E5575B3051413B", reader.getFileHash());
    }

    @Test
    public final void testGetSamples() throws IOException {
        final GenemapperFileReader reader = new GenemapperFileReader(getTestFile(GENEMAPPER_FILE_WITHOUT_HEIGHTS));
        reader.getCaseNumber(); // to test initialization of the reader.

        final Collection<Sample> samples = reader.getSamples();
        assertNotNull(samples);
        assertEquals(4, samples.size());

        for (final Sample sample : samples) {
            assertTrue(sample.getName() + " does not match regex AAAA0000NL#01_Rep\\\\d", sample.getName().matches("AAAA0000NL#01_Rep\\d"));

            final Collection<Locus> loci = sample.getLoci();
            assertNotNull(loci);

            final ArrayList<String> expectedLoci = new ArrayList<>(Arrays.asList("D16S539", "D18S51", "D21S11", "D3S1358", "D8S1179", "FGA", "TH01", "VWA", "D19S433", "D2S1338", "D1S1656", "D2S441", "D10S1248", "D12S391", "D22S1045", "AMEL"));
            for (final Locus locus : loci) {
                final String locusName = locus.getName();
                assertTrue("Unexpected locus in sample " + sample.getName() + ": " + locusName, expectedLoci.contains(locusName));
                expectedLoci.remove(locusName);
            }
            assertTrue("Expected loci not found: " + expectedLoci, expectedLoci.isEmpty());
            assertEquals("File hash for reader and sample " + sample.getName() + " are not equal!", reader.getFileHash(), sample.getSourceFileHash());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testGetSamplesDifferentCaseNumbers() throws IOException {
        final GenemapperFileReader reader = new GenemapperFileReader(getTestFile(GENEMAPPER_FILE_DIFFERENT_CASE_NUMBERS));
        reader.getCaseNumber(); // to test initialization of the reader.
    }

    @Test
    public final void testGetSamplesWithHeights() throws IOException {
        final GenemapperFileReader reader = new GenemapperFileReader(getTestFile(GENEMAPPER_FILE_WITH_HEIGHTS));
        reader.getCaseNumber(); // to test initialization of the reader.

        final Collection<Sample> samples = reader.getSamples();
        assertNotNull(samples);
        assertEquals(4, samples.size());

        for (final Sample sample : samples) {
            assertTrue(sample.getName() + " does not match regex 'AAAA0000NL#02_Rep\\\\d'", sample.getName().matches("AAAA0000NL#02_Rep\\d"));

            final Collection<Locus> loci = sample.getLoci();
            assertNotNull(loci);

            final ArrayList<String> expectedLoci = new ArrayList<>(Arrays.asList("D16S539", "D18S51", "D21S11", "D3S1358", "D8S1179", "FGA", "TH01", "VWA", "D19S433", "D2S1338", "D1S1656", "D2S441", "D10S1248", "D12S391", "D22S1045", "AMEL"));
            for (final Locus locus : loci) {
                final String locusName = locus.getName();
                assertTrue("Unexpected locus in sample " + sample.getName() + ": " + locusName, expectedLoci.contains(locusName));
                expectedLoci.remove(locusName);
            }
            assertTrue("Expected loci not found: " + expectedLoci, expectedLoci.isEmpty());
        }
    }


    @Test
    public final void testGetSamplesWithHeightsAndOL() throws IOException {
        final GenemapperFileReader reader = new GenemapperFileReader(getTestFile(GENEMAPPER_FILE_WITH_HEIGHTS_AND_OFFLADDER));
        reader.getCaseNumber(); // to test initialization of the reader.

        final Collection<Sample> samples = reader.getSamples();
        assertNotNull(samples);
        assertEquals(1, samples.size());

        for (final Sample sample : samples) {
            assertEquals("AAAA0000NL#01", sample.getName());

            final Collection<Locus> loci = sample.getLoci();
            assertNotNull(loci);

            final ArrayList<String> expectedLoci = new ArrayList<>(Arrays.asList("D16S539", "D18S51", "D21S11", "D3S1358", "D8S1179", "FGA", "TH01", "VWA", "D19S433", "D2S1338", "D1S1656", "D2S441", "D10S1248", "D12S391", "D22S1045", "AMEL"));
            for (final Locus locus : loci) {
                final String locusName = locus.getName();
                assertTrue("Unexpected locus in sample " + sample.getName() + ": " + locusName, expectedLoci.contains(locusName));
                expectedLoci.remove(locusName);
            }
            assertTrue("Expected loci not found: " + expectedLoci, expectedLoci.isEmpty());
        }
    }

    @Test
    public final void testGetCaseNumber() throws IOException {
        final GenemapperFileReader reader = new GenemapperFileReader(getTestFile(GENEMAPPER_FILE_WITHOUT_HEIGHTS));
        assertEquals("00000000000", reader.getCaseNumber());
    }

    private File getTestFile(final String fileName) {
        final URL url = getClass().getResource(fileName);
        return new File(url.getPath());
    }
}
