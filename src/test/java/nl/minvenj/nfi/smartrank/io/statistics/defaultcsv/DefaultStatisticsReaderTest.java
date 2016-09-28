package nl.minvenj.nfi.smartrank.io.statistics.defaultcsv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.io.CSVReader;

public class DefaultStatisticsReaderTest {

    private static final String FREQUENCIES_FILE_HASH = "SHA-1/DF2790826F03ECAF9C41DD202809AEA62EACE00D";
    private static final String FREQUENCIES_FILE = "STRBASE_frequencies_2015-02-09_Europe.csv";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testDefaultStatisticsReaderString() throws FileNotFoundException, MalformedURLException, IOException {
        final DefaultStatisticsReader reader = new DefaultStatisticsReader(getTestFile(FREQUENCIES_FILE).getAbsolutePath());
        final PopulationStatistics statistics = reader.getStatistics();
        assertNotNull(statistics);
        assertEquals(FREQUENCIES_FILE_HASH, statistics.getFileHash());
    }

    @Test
    public final void testDefaultStatisticsReaderStringInputStream() throws IOException {
        final DefaultStatisticsReader reader = new DefaultStatisticsReader("", getClass().getResourceAsStream(FREQUENCIES_FILE));
        final PopulationStatistics statistics = reader.getStatistics();
        assertNotNull(statistics);
        assertEquals(FREQUENCIES_FILE_HASH, statistics.getFileHash());
    }

    @Test
    public final void testDefaultStatisticsReaderStringCSVReader() throws IOException {
        final CSVReader csvReader = new CSVReader(getTestFile(FREQUENCIES_FILE));
        final DefaultStatisticsReader reader = new DefaultStatisticsReader("", csvReader);
        final PopulationStatistics statistics = reader.getStatistics();
        assertNotNull(statistics);
        assertEquals(FREQUENCIES_FILE_HASH, statistics.getFileHash());
    }

    private File getTestFile(final String fileName) {
        final URL url = getClass().getResource(fileName);
        return new File(url.getPath());
    }
}
