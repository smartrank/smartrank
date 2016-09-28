package nl.minvenj.nfi.smartrank.io.samples.lrmix;

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

public class LRMixFileReaderTest {

    private static final String TSV_FILE = "suspect.tsv";
    private static final String CSV_FILE = "suspect.csv";
    private static final String BROKEN_FILE = "suspect-broken.csv";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testLRMixFileReader() {
        final LRMixFileReader reader = new LRMixFileReader(getTestFile(CSV_FILE));
        assertEquals(CSV_FILE, reader.getFile().getName());
    }

    @Test
    public final void testGetSamples() throws IOException {
        final LRMixFileReader reader = new LRMixFileReader(getTestFile(CSV_FILE));
        reader.getCaseNumber(); // to test initialization of the reader.

        final Collection<Sample> samples = reader.getSamples();
        assertNotNull(samples);
        assertEquals(1, samples.size());

        final Sample sample = samples.iterator().next();
        assertEquals("OVXV0389BE#98", sample.getName());

        final Collection<Locus> loci = sample.getLoci();
        assertNotNull(loci);

        final ArrayList<String> expectedLoci = new ArrayList<>(Arrays.asList("TPOX", "D5S818", "SE33", "D13S317", "CSF1PO", "D16S539", "D18S51", "D21S11", "D3S1358", "D8S1179", "FGA", "TH01", "VWA", "D19S433", "D2S1338", "D10S1248"));
        for (final Locus locus : loci) {
            final String locusName = locus.getName();
            assertTrue("Unexpected locus in sample: " + locusName, expectedLoci.contains(locusName));
            expectedLoci.remove(locusName);
        }
        assertTrue("Expected loci not found: " + expectedLoci, expectedLoci.isEmpty());
    }

    @Test
    public final void testGetCaseNumber() {
        final LRMixFileReader reader = new LRMixFileReader(getTestFile(CSV_FILE));
        assertEquals("", reader.getCaseNumber());
    }

    @Test
    public final void testGetFileHash() throws IOException {
        final LRMixFileReader reader = new LRMixFileReader(getTestFile(CSV_FILE));
        assertEquals("SHA-1/82E54551B5950F4B78F1CF0A6DE645FAF70089DE", reader.getFileHash());
    }

    private File getTestFile(final String fileName) {
        final URL url = getClass().getResource(fileName);
        return new File(url.getPath());
    }
}
