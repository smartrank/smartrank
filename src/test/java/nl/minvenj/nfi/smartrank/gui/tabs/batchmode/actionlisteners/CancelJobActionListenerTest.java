package nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchJobInfo;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchModePanel;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchProcessingTable;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.ScanStatus;

@RunWith(MockitoJUnitRunner.class)
public class CancelJobActionListenerTest {

    @Mock
    BatchModePanel _mockBatchModePanel;

    @Mock
    BatchProcessingTable _mockTable;

    @Mock
    BatchJobInfo _mockBatchInfo;

    @Test
    public final void test() {
        final CancelJobActionListener listener = new CancelJobActionListener(_mockBatchModePanel);

        when(_mockBatchModePanel.getFilesTable()).thenReturn(_mockTable);
        final File fileBeforeMove = new File("file before move");
        final File fileAfterMove = new File("file after move");
        final File failedFolder = new File("FailedFolder");

        when(_mockBatchModePanel.moveFileToDatedFolder(any(), any())).thenReturn(fileAfterMove);
        when(_mockBatchModePanel.getFailedFolder()).thenReturn(failedFolder);

        when(_mockTable.getSelectedRow()).thenReturn(1);
        when(_mockTable.getValueAt(anyInt(), eq(0))).thenReturn(fileBeforeMove);
        when(_mockTable.getValueAt(anyInt(), eq(1))).thenReturn(_mockBatchInfo);

        listener.actionPerformed(null);
        verify(_mockBatchModePanel, times(1)).moveFileToDatedFolder(fileBeforeMove, failedFolder);
        verify(_mockTable, times(1)).setValueAt(fileAfterMove, 1, 0);
        verify(_mockBatchInfo, times(1)).setStatus(ScanStatus.CANCELLED);
        verify(_mockTable, times(1)).setValueAt(_mockBatchInfo, 1, 1);
        verify(_mockTable, times(1)).setValueAt(_mockBatchInfo, 1, 2);
    }

}
