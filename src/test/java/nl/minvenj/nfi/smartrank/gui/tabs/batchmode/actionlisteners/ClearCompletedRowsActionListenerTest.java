package nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchJobInfo;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchProcessingTable;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.ScanStatus;

@RunWith(MockitoJUnitRunner.class)
public class ClearCompletedRowsActionListenerTest {

    @Mock
    BatchProcessingTable _mockTable;

    @Mock
    BatchJobInfo _mockBatchInfoSucceeded;

    @Mock
    BatchJobInfo _mockBatchInfoFailed;

    @Mock
    BatchJobInfo _mockBatchInfoPending;

    @Mock
    BatchJobInfo _mockBatchInfoInterrupted;

    @Mock
    BatchJobInfo _mockBatchInfoProcessing;

    @Test
    public final void test() {
        final ClearCompletedRowsActionListener listener = new ClearCompletedRowsActionListener(_mockTable);

        when(_mockTable.getRowCount()).thenReturn(5);
        when(_mockTable.getValueAt(0, 1)).thenReturn(_mockBatchInfoSucceeded);
        when(_mockTable.getValueAt(1, 1)).thenReturn(_mockBatchInfoFailed);
        when(_mockTable.getValueAt(2, 1)).thenReturn(_mockBatchInfoPending);
        when(_mockTable.getValueAt(3, 1)).thenReturn(_mockBatchInfoInterrupted);
        when(_mockTable.getValueAt(4, 1)).thenReturn(_mockBatchInfoProcessing);

        when(_mockBatchInfoSucceeded.getStatus()).thenReturn(ScanStatus.SUCCEEDED);
        when(_mockBatchInfoFailed.getStatus()).thenReturn(ScanStatus.FAILED);
        when(_mockBatchInfoPending.getStatus()).thenReturn(ScanStatus.PENDING);
        when(_mockBatchInfoInterrupted.getStatus()).thenReturn(ScanStatus.INTERRUPTED);
        when(_mockBatchInfoProcessing.getStatus()).thenReturn(ScanStatus.PROCESSING);

        listener.actionPerformed(null);

        verify(_mockTable, times(1)).removeRow(0);
        verify(_mockTable, times(1)).removeRow(1);
        verify(_mockTable, times(0)).removeRow(2);
        verify(_mockTable, times(0)).removeRow(3);
        verify(_mockTable, times(0)).removeRow(4);
    }

}
