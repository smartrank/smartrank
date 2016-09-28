package nl.minvenj.nfi.smartrank.io.samples.codis;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;

public class CodisFileReaderTest {

    private static final String XML_FILE = "Codis-Single-Profile.xml";

    @Rule
    public ExpectedException _expected = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testCodisFileReader() {
        final CodisFileReader reader = new CodisFileReader(getTestFile(XML_FILE));
        assertEquals(XML_FILE, reader.getFile().getName());
    }

    @Test
    public final void testGetFileHash() throws IOException {
        final CodisFileReader reader = new CodisFileReader(getTestFile(XML_FILE));
        assertEquals("SHA-1/F756EB4B479DE291A015D9D1D029014320121CF1", reader.getFileHash());
    }

    @Test
    public final void testGetSamples() throws IOException {
        final CodisFileReader reader = new CodisFileReader(getTestFile(XML_FILE));
        reader.getCaseNumber(); // to test initialization of the reader.

        final Collection<Sample> samples = reader.getSamples();
        assertNotNull(samples);
        assertEquals(1, samples.size());

        final Sample sample = samples.iterator().next();
        assertEquals("ABCD1234NL#01", sample.getName());

        final Collection<Locus> loci = sample.getLoci();
        assertNotNull(loci);

        final ArrayList<String> expectedLoci = new ArrayList<>(Arrays.asList("D16S539", "D18S51", "D21S11", "D3S1358", "D8S1179", "FGA", "TH01", "VWA", "AMELOGENIN", "D19S433", "D2S1338", "D1S1656", "D2S441", "D10S1248", "D12S391", "D22S1045"));
        for (final Locus locus : loci) {
            final String locusName = locus.getName();
            assertTrue("Unexpected locus in sample: " + locusName, expectedLoci.contains(locusName));
            expectedLoci.remove(locusName);
        }
        assertTrue("Expected loci not found: " + expectedLoci, expectedLoci.isEmpty());
    }

    @Test
    public final void testGetCaseNumber() throws IOException {
        final CodisFileReader reader = new CodisFileReader(getTestFile(XML_FILE));
        assertEquals("2099.12.31.000", reader.getCaseNumber());
    }

    @Test
    public final void testReadNonCodisXMLFile() throws IOException {
        _expected.expectMessage("This is not a Codis xml file!");
        final CodisFileReader reader = new CodisFileReader(getTestFile("errorhandling/not-a-codis-file.xml"));
        reader.getSamples();
    }

    @Test
    public final void testReadNoProfilesCodisXMLFile() throws IOException {
        _expected.expectMessage("No specimens found in this file!");
        final CodisFileReader reader = new CodisFileReader(getTestFile("errorhandling/no-profiles.xml"));
        reader.getSamples();
    }

    @Test
    public final void testReadBrokenLocusCodisXMLFile() throws IOException {
        _expected.expectMessage("This file is corrupt!");
        final CodisFileReader reader = new CodisFileReader(getTestFile("errorhandling/broken-locus.xml"));
        reader.getSamples();
    }

    @Test
    public final void testReadBrokenAlleleCodisXMLFile() throws IOException {
        _expected.expectMessage("This file is corrupt!");
        final CodisFileReader reader = new CodisFileReader(getTestFile("errorhandling/broken-allele.xml"));
        reader.getSamples();
    }

    private File getTestFile(final String name) {
        final URL url = getClass().getResource(name);
        if (url == null) {
            throw new IllegalArgumentException("File not found: " + name);
        }
        return new File(url.getPath());
    }
}
