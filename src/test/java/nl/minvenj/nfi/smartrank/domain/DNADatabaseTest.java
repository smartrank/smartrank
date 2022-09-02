package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import nl.minvenj.nfi.smartrank.io.databases.DatabaseValidationEventListener;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.messages.data.EnabledLociMessage;
import nl.minvenj.nfi.smartrank.messages.data.ProsecutionHypothesisMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

@RunWith(Parameterized.class)
public class DNADatabaseTest {

    @Parameters(name = "{0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
            {"database_codis_2records_lowerascii.csv", "CODIS", "SHA-1/74D9EC71C1EEB4878FD0356DD188D53C0FD9066D", 2, 2, 0},
            {"database_codis_3records_1error.csv", "CODIS", "SHA-1/3B148EABD20354C6B9316F38F6DBAEE2A69F6EA6", 3, 3, 1},
            {"database_codis_4records_1error_1empty.csv", "CODIS", "SHA-1/8B9DB891FCFABBAF8E26469888E221BC1524DC8A", 4, 3, 1},
            {"database_codis_5002records_lowerascii.csv", "CODIS", "SHA-1/909188332F754E8622882E73F3BFC777401EFE16", 5002, 5002, 0},
            {"database_codis_2records_utf8withBOM.csv", "CODIS", "SHA-1/7FD3192E2532CCCD7147894DF491408F1C74770B", 2, 2, 0},
            {"database_codis_generated_300000.csv", "CODIS", "SHA-1/79F4D9A81857EE250B937036F68EEE873A3E28CD", 299534, 299534, 138932}
        });
    }

    @Parameter(0)
    public String _connectString;

    @Parameter(1)
    public String _format;

    @Parameter(2)
    public String _fileHash;

    @Parameter(3)
    public int _totalRecordCount;

    @Parameter(4)
    public int _iteratedRecordCount;

    @Parameter(5)
    public int _expectedProblemCount;

    @Test
    public final void testDNADatabaseFile() {
        final DNADatabase db = new DNADatabase(new File(_connectString));
        assertNotNull(db.getConnectString());
        assertTrue(db.getConnectString().contains(_connectString));
    }

    @Test
    public final void testDNADatabaseString() {
        final DNADatabase db = new DNADatabase(new File(_connectString));
        assertTrue(db.getConnectString().endsWith(_connectString));
    }

    @Test
    public final void testValidateAndIterate() throws IOException, InterruptedException {
        final DNADatabase db = new DNADatabase(new File(getClass().getResource(_connectString).getPath()));
        final StringBuilder encounteredProblems = new StringBuilder();
        final AtomicInteger problemCounter = new AtomicInteger(0);

        MessageBus.getInstance().send("DNADatabaseTest", new EnabledLociMessage(Arrays.asList("TH01", "SE33", "D8S1179", "D10S1248", "VWA", "Dummy")));
        MessageBus.getInstance().send("DNADatabaseTest", new ProsecutionHypothesisMessage(new ProsecutionHypothesis()));
        MessageBus.getInstance().send("DNADatabaseTest", new AnalysisParametersMessage(new AnalysisParameters()));

        db.validate(new DatabaseValidationEventListener() {

            @Override
            public void onProgress(final long current, final long max) {
            }

            @Override
            public void onProblem(final String specimen, final String locus, final String message) {
                encounteredProblems.append("Specimen " + specimen + "." + locus + ": " + message).append("\r\n");
                problemCounter.incrementAndGet();
            }
        });

        assertEquals("Expected " + _expectedProblemCount + " problems but found " + problemCounter.intValue() + " :" + encounteredProblems.toString(), _expectedProblemCount, problemCounter.intValue());
        assertEquals("Total Record Count was " + db.getRecordCount() + " instead of the expected ", _totalRecordCount, _totalRecordCount, db.getRecordCount());
        assertEquals("Format was " + db.getFormatName() + " instead of the expected " + _format, _format, db.getFormatName());
        assertEquals("File Hash was " + db.getFileHash() + " instead of the expected " + _fileHash, _fileHash, db.getFileHash());

        final Iterator<Sample> iterator = db.iterator(null);

        try {
            iterator.remove();
            fail("The Sample iterator should not support removal!");
        }
        catch (final UnsupportedOperationException usoe) {
            // The sample iterator does not support removal. An exception here is OK
        }

        int count = 0;
        assertNotNull(iterator);
        while (iterator.hasNext()) {
            final Sample sample = iterator.next();
            assertNotNull(sample);
            count++;
        }
        assertEquals("Iterated over " + count + " records instead of the expected " + _iteratedRecordCount, _iteratedRecordCount, count);
    }
}
