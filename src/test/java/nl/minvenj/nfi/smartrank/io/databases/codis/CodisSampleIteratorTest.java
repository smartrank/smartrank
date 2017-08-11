package nl.minvenj.nfi.smartrank.io.databases.codis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import nl.minvenj.nfi.smartrank.analysis.ExcludedProfile;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.CSVReader;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.messages.data.EnabledLociMessage;
import nl.minvenj.nfi.smartrank.messages.data.ProsecutionHypothesisMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

@RunWith(Parameterized.class)
public class CodisSampleIteratorTest {

    @Parameters
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
            {"database_codis_generated_44.csv", 44}
        });
    }

    @Parameter(0)
    public String _fileName;

    @Parameter(1)
    public int _expectedCount;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testLoopOverSamples() throws IOException {
        MessageBus.getInstance().send("DNADatabaseTest", new EnabledLociMessage(Arrays.asList("D10S1248", "VWA", "Dummy")));
        MessageBus.getInstance().send("DNADatabaseTest", new AnalysisParametersMessage(new AnalysisParameters()));
        MessageBus.getInstance().send("DNADatabaseTest", new ProsecutionHypothesisMessage(new ProsecutionHypothesis()));
        final CodisSampleIterator iterator = new CodisSampleIterator(new CSVReader(getClass().getResource(_fileName).getFile()), _expectedCount, new ArrayList<ExcludedProfile>());

        final ArrayList<String> names = new ArrayList<>();

        int count = 0;
        while (iterator.hasNext()) {
            final Sample sample = iterator.next();

            assertNotNull("CodisSampleIterator.next() returned null!");
            assertFalse("Encountered duplicate sample being returned: " + sample.getName(), names.contains(sample.getName()));
            names.add(sample.getName());
            count++;
        }
        assertEquals(_expectedCount, count);
    }
}
