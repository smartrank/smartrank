package nl.minvenj.nfi.smartrank.gui.tabs.batchmode;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;

@RunWith(MockitoJUnitRunner.class)
public class BatchJobInfoTest {

    @Mock
    SearchCriteriaReader _mockSearch;

    @Mock
    SearchResults _mockSearchResults;

    @Rule
    public ExpectedException _expected = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testBatchJobInfoFileReaderStatus_001() {
        _expected.expect(IllegalArgumentException.class);
        _expected.expectMessage("sourceFile");
        new BatchJobInfo(null, null, ScanStatus.PENDING);
    }

    @Test
    public final void testBatchJobInfoFileReaderStatus_101() {
        _expected.expect(IllegalArgumentException.class);
        _expected.expectMessage("reader");
        new BatchJobInfo(new File("notnull"), null, ScanStatus.PENDING);
    }

    @Test
    public final void testBatchJobInfoFileReaderStatus_110() {
        _expected.expect(IllegalArgumentException.class);
        _expected.expectMessage("status");
        new BatchJobInfo(new File("notnull"), _mockSearch, null);
    }

    @Test
    public final void testBatchJobInfoFileReaderStatus_111() {
        new BatchJobInfo(new File("notnull"), _mockSearch, ScanStatus.PENDING);
    }

    @Test
    public final void testBatchJobInfoFileReaderString_001() {
        _expected.expect(IllegalArgumentException.class);
        _expected.expectMessage("sourceFile");
        new BatchJobInfo(null, null, "");
    }

    @Test
    public final void testBatchJobInfoFileReaderString_101() {
        _expected.expect(IllegalArgumentException.class);
        _expected.expectMessage("status");
        new BatchJobInfo(new File("notnull"), null, "");
    }

    @Test
    public final void testBatchJobInfoFileReaderString_110() {
        new BatchJobInfo(new File("notnull"), ScanStatus.PENDING, null);
    }

    @Test
    public final void testBatchJobInfoFileReaderString_111() {
        new BatchJobInfo(new File("notnull"), ScanStatus.PENDING, "");
    }

    @Test
    public final void testGetReaderNull() {
        final BatchJobInfo batchJobInfo = new BatchJobInfo(new File("notnull"), ScanStatus.PENDING, "");
        assertThat(batchJobInfo.getReader(), is(nullValue()));
    }

    @Test
    public final void testGetReaderNotNull() {
        final BatchJobInfo batchJobInfo = new BatchJobInfo(new File("notnull"), _mockSearch, ScanStatus.PENDING);
        assertThat(batchJobInfo.getReader(), equalTo(_mockSearch));
    }

    @Test
    public final void testSetGetErrorMessage() {
        final BatchJobInfo batchJobInfo = new BatchJobInfo(new File("notnull"), _mockSearch, ScanStatus.PENDING);
        assertThat(batchJobInfo.getErrorMessage(), is(nullValue()));
        batchJobInfo.setErrorMessage("Test");
        assertThat(batchJobInfo.getErrorMessage(), equalTo("Test"));
        batchJobInfo.setErrorMessage(null);
        assertThat(batchJobInfo.getErrorMessage(), is(nullValue()));
    }

    @Test
    public final void testGetErrorMessage() {
        final BatchJobInfo batchJobInfo = new BatchJobInfo(new File("notnull"), ScanStatus.PENDING, "testGetErrorMessage");
        assertThat(batchJobInfo.getErrorMessage(), equalTo("testGetErrorMessage"));
    }

    @Test
    public final void testSetGetStatus() {
        final BatchJobInfo batchJobInfo = new BatchJobInfo(new File("notnull"), _mockSearch, ScanStatus.PENDING);
        assertThat(batchJobInfo.getStatus(), is(ScanStatus.PENDING));

        for (final ScanStatus status : ScanStatus.values()) {
            batchJobInfo.setStatus(status);
            assertThat(batchJobInfo.getStatus(), equalTo(status));
        }
    }

    @Test
    public final void testGetStatusTimestamp() throws ParseException {
        final long now = System.currentTimeMillis();
        final BatchJobInfo batchJobInfo = new BatchJobInfo(new File("notnull"), _mockSearch, ScanStatus.PENDING);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String timestamp = batchJobInfo.getStatusTimestamp();
        assertThat(timestamp, is(not(nullValue())));
        final long time = sdf.parse(timestamp).getTime();
        assertThat(time - now, lessThan(100L));
    }

    @Test
    public final void testIsSucceeded() {
        final BatchJobInfo batchJobInfo = new BatchJobInfo(new File("notnull"), _mockSearch, ScanStatus.PENDING);
        for (final ScanStatus status : ScanStatus.values()) {
            batchJobInfo.setStatus(status);
            assertThat(batchJobInfo.isSucceeded(), is(status == ScanStatus.SUCCEEDED));
        }
    }

    @Test
    public final void testSetGetResultsSucceeded() {
    	final BatchJobInfo batchJobInfo = new BatchJobInfo(new File("notnull"), _mockSearch, ScanStatus.PENDING);
        when(_mockSearchResults.isSucceeded()).thenReturn(true);
        when(_mockSearchResults.getLogFileName()).thenReturn("DummyLogName");
        batchJobInfo.setResults(_mockSearchResults);
        assertThat(batchJobInfo.getLogFileName(), is("DummyLogName"));
        assertThat(batchJobInfo.getReportFileName(), is(nullValue()));
        assertThat(batchJobInfo.getStatus(), is(ScanStatus.SUCCEEDED));
    }

    @Test
    public final void testSetGetResultsFailed() {
        final BatchJobInfo batchJobInfo = new BatchJobInfo(new File("notnull"), _mockSearch, ScanStatus.PENDING);
        when(_mockSearchResults.getFailureReason()).thenReturn(new Exception("testSetGetResultsFailed"));
        when(_mockSearchResults.getLogFileName()).thenReturn("DummyLogName");
        batchJobInfo.setResults(_mockSearchResults);
        assertThat(batchJobInfo.getLogFileName(), is("DummyLogName"));
        assertThat(batchJobInfo.getReportFileName(), is(nullValue()));
        assertThat(batchJobInfo.getStatus(), is(ScanStatus.FAILED));
        assertThat(batchJobInfo.getErrorMessage(), is("testSetGetResultsFailed"));
    }

    @Test
    public final void testSetGetResultsInterrupted() {
        final BatchJobInfo batchJobInfo = new BatchJobInfo(new File("notnull"), _mockSearch, ScanStatus.PENDING);
        when(_mockSearchResults.isInterrupted()).thenReturn(true);
        when(_mockSearchResults.getLogFileName()).thenReturn("DummyLogName");
        batchJobInfo.setResults(_mockSearchResults);
        assertThat(batchJobInfo.getLogFileName(), is("DummyLogName"));
        assertThat(batchJobInfo.getReportFileName(), is(nullValue()));
        assertThat(batchJobInfo.getStatus(), is(ScanStatus.INTERRUPTED));
    }

    @Test
    public final void testSetGetResultsNull() {
        final BatchJobInfo batchJobInfo = new BatchJobInfo(new File("notnull"), _mockSearch, ScanStatus.PENDING);
        batchJobInfo.setResults(null);
        assertThat(batchJobInfo.getLogFileName(), is(nullValue()));
        assertThat(batchJobInfo.getReportFileName(), is(nullValue()));
        assertThat(batchJobInfo.getStatus(), is(ScanStatus.PENDING));
    }

    @Test
    public final void testSetGetFileName() {
        final BatchJobInfo batchJobInfo = new BatchJobInfo(new File("notnull"), _mockSearch, ScanStatus.PENDING);
        assertThat(batchJobInfo.getFileName(), endsWith("notnull"));
        batchJobInfo.setFileName("anotherfilename");
        assertThat(batchJobInfo.getFileName(), endsWith("anotherfilename"));
    }

}
