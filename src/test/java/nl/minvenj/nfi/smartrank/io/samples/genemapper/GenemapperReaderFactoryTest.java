package nl.minvenj.nfi.smartrank.io.samples.genemapper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import nl.minvenj.nfi.smartrank.io.samples.SampleFileReader;
import nl.minvenj.nfi.smartrank.io.samples.genemapper.GenemapperFileReader;
import nl.minvenj.nfi.smartrank.io.samples.genemapper.GenemapperReaderFactory;

public class GenemapperReaderFactoryTest {

    private static final String GENEMAPPER_FILE = "GeneMapperExample.txt";
    private static final String GENEMAPPER_FILE_BROKEN_HEADER = "GeneMapperExampleBrokenHeader.txt";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testAccepts() {
        final GenemapperReaderFactory factory = new GenemapperReaderFactory();
        assertTrue(factory.accepts(getTestFile(GENEMAPPER_FILE)));
        assertFalse(factory.accepts(getTestFile(GENEMAPPER_FILE_BROKEN_HEADER)));
        assertFalse(factory.accepts(new File("DoesNotExist")));
    }

    @Test
    public final void testCreate() {
        final GenemapperReaderFactory factory = new GenemapperReaderFactory();
        final SampleFileReader fileReaderCSV = factory.create(getTestFile(GENEMAPPER_FILE));
        assertNotNull(fileReaderCSV);
        assertTrue(fileReaderCSV instanceof GenemapperFileReader);
    }

    private File getTestFile(final String fileName) {
        final URL url = getClass().getResource(fileName);
        return new File(url.getPath());
    }
}
