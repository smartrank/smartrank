package nl.minvenj.nfi.smartrank.io.samples.lrmix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LRMixReaderFactoryTest {

    @Parameters(name = "{3} - {0}")
    public static Object[][] getParameters() {
        return new Object[][]{
            {"suspect.tsv", true, "TSV file"},
            {"suspect.csv", true, "CSV file"},
            {"suspect-broken.csv", false, "Broken CSV file"},
            {"DoesNotExist", false, "Non-existent file"},
            {"suspect2.csv", true, "CSV with alternate sample name field"},
            {"suspect-utf8-bom.csv", true, "CSV with little endian byte order marker"}
        };
    }

    @Parameter(0)
    public String fileName;

    @Parameter(1)
    public boolean accepted;

    @Parameter(2)
    public String description;

    public static final LRMixReaderFactory FACTORY = new LRMixReaderFactory();

    @Test
    public final void testAccepts() {
        assertThat(FACTORY.accepts(getTestFile(fileName)), equalTo(accepted));
    }

    @Test
    public final void testCreate() {
        assertThat(FACTORY.create(getTestFile(fileName)), instanceOf(LRMixFileReader.class));
    }

    private File getTestFile(final String fileName) {
        final URL url = getClass().getResource(fileName);
        if (url == null) {
            return new File(fileName);
        }
        return new File(url.getPath());
    }
}
