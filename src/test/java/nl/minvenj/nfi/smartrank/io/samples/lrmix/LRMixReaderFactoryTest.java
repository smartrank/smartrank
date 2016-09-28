package nl.minvenj.nfi.smartrank.io.samples.lrmix;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import nl.minvenj.nfi.smartrank.io.samples.SampleFileReader;
import nl.minvenj.nfi.smartrank.io.samples.lrmix.LRMixFileReader;
import nl.minvenj.nfi.smartrank.io.samples.lrmix.LRMixReaderFactory;

public class LRMixReaderFactoryTest {

    private static final String TSV_FILE = "suspect.tsv";
    private static final String CSV_FILE = "suspect.csv";
    private static final String BROKEN_FILE = "suspect-broken.csv";

    @Test
    public final void testAccepts() {
        final LRMixReaderFactory factory = new LRMixReaderFactory();
        assertTrue(factory.accepts(getTestFile(TSV_FILE)));
        assertFalse(factory.accepts(getTestFile(BROKEN_FILE)));
        assertTrue(factory.accepts(getTestFile(CSV_FILE)));
        assertFalse(factory.accepts(new File("DoesNotExist")));
    }

    @Test
    public final void testCreate() {
        final LRMixReaderFactory factory = new LRMixReaderFactory();
        final SampleFileReader fileReaderCSV = factory.create(getTestFile(CSV_FILE));
        assertNotNull(fileReaderCSV);
        assertTrue(fileReaderCSV instanceof LRMixFileReader);

        final SampleFileReader fileReaderTSV = factory.create(getTestFile(TSV_FILE));
        assertNotNull(fileReaderTSV);

        final SampleFileReader fileReaderBROKEN = factory.create(getTestFile(BROKEN_FILE));
        assertNotNull(fileReaderBROKEN);
    }

    private File getTestFile(final String fileName) {
        final URL url = getClass().getResource(fileName);
        return new File(url.getPath());
    }
}
