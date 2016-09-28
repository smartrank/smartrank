package nl.minvenj.nfi.smartrank.io.samples.codis;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import nl.minvenj.nfi.smartrank.io.samples.SampleFileReader;
import nl.minvenj.nfi.smartrank.io.samples.codis.CodisFileReader;
import nl.minvenj.nfi.smartrank.io.samples.codis.CodisImportFileReaderFactory;

public class CodisImportFileReaderFactoryTest {

    private static final String XML_FILE = "Codis-Single-Profile.xml";
    private static final String CSV_FILE = "sample.csv";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testAccepts() {
        final CodisImportFileReaderFactory codisImportFileReaderFactory = new CodisImportFileReaderFactory();
        codisImportFileReaderFactory.accepts(getTestFile(XML_FILE));
        codisImportFileReaderFactory.accepts(getTestFile(CSV_FILE));
    }

    private File getTestFile(final String fileName) {
        final URL url = getClass().getResource(fileName);
        return new File(url.getPath());
    }

    @Test
    public final void testCreate() throws IOException {
        final CodisImportFileReaderFactory codisImportFileReaderFactory = new CodisImportFileReaderFactory();
        final SampleFileReader sampleFileReader = codisImportFileReaderFactory.create(getTestFile(XML_FILE));
        assertNotNull(sampleFileReader);
        assertTrue(sampleFileReader instanceof CodisFileReader);
    }
}
