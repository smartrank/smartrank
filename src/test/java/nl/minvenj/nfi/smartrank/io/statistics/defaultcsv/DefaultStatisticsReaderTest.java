package nl.minvenj.nfi.smartrank.io.statistics.defaultcsv;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.io.CSVReader;

public class DefaultStatisticsReaderTest {

    // Depending on Git settings, the line endings of the checked-out file could have been changed from CR/LF to CR or vice versa
    // We will compare the file's hash to the reference hash of both EOL conventions. If one of them matches, we're good.
    private static final String FREQUENCIES_FILE_HASH_CRLF = "SHA-1/4AC5E4B6471EFCFFB8BED2E1391F7AC690C44012";
    private static final String FREQUENCIES_FILE_HASH_CR = "SHA-1/DF2790826F03ECAF9C41DD202809AEA62EACE00D";
    private static final String FREQUENCIES_FILE = "STRBASE_frequencies_2015-02-09_Europe.csv";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testDefaultStatisticsReaderString() throws FileNotFoundException, MalformedURLException, IOException {
        final DefaultStatisticsReader reader = new DefaultStatisticsReader(getTestFile(FREQUENCIES_FILE).getAbsolutePath());
        final PopulationStatistics statistics = reader.getStatistics();
        assertNotNull(statistics);

        assertThat(statistics.getFileHash(), Matchers.either(Matchers.is(FREQUENCIES_FILE_HASH_CRLF)).or(Matchers.is(FREQUENCIES_FILE_HASH_CR)));
    }

    @Test
    public final void testDefaultStatisticsReaderStringInputStream() throws IOException {
        final DefaultStatisticsReader reader = new DefaultStatisticsReader("", getClass().getResourceAsStream(FREQUENCIES_FILE));
        final PopulationStatistics statistics = reader.getStatistics();
        assertNotNull(statistics);
        assertThat(statistics.getFileHash(), Matchers.either(Matchers.is(FREQUENCIES_FILE_HASH_CRLF)).or(Matchers.is(FREQUENCIES_FILE_HASH_CR)));
    }

    @Test
    public final void testDefaultStatisticsReaderStringCSVReader() throws IOException {
        final CSVReader csvReader = new CSVReader(getTestFile(FREQUENCIES_FILE));
        final DefaultStatisticsReader reader = new DefaultStatisticsReader("", csvReader);
        final PopulationStatistics statistics = reader.getStatistics();
        assertNotNull(statistics);
        assertThat(statistics.getFileHash(), Matchers.either(Matchers.is(FREQUENCIES_FILE_HASH_CRLF)).or(Matchers.is(FREQUENCIES_FILE_HASH_CR)));
    }

    private File getTestFile(final String fileName) {
        final URL url = getClass().getResource(fileName);
        return new File(url.getPath());
    }
}
