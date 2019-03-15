package nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchJobInfo;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchModePanel;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchProcessingTable;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.ScanStatus;

@RunWith(MockitoJUnitRunner.class)
public class RestartJobActionListenerTest {

    @Mock
    private BatchModePanel _mockPanel;

    @Mock
    private BatchProcessingTable _mockTable;

    @Mock
    private BatchJobInfo _mockInfo;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void test() {
        when(_mockPanel.getFilesTable()).thenReturn(_mockTable);
        final File fileBeforeMove = new File("file before move");
        final File fileAfterMove = new File("file after move");
        final File targetFolder = new File("TargetFolder");

        when(_mockPanel.moveFileToFolder(any(), any())).thenReturn(fileAfterMove);
        when(_mockPanel.getInputFolder()).thenReturn(targetFolder);

        when(_mockTable.getSelectedRow()).thenReturn(1);
        when(_mockTable.getValueAt(anyInt(), eq(0))).thenReturn(fileBeforeMove);
        when(_mockTable.getValueAt(anyInt(), eq(1))).thenReturn(_mockInfo);

        new RestartJobActionListener(_mockPanel).actionPerformed(null);

        verify(_mockPanel, times(1)).moveFileToFolder(fileBeforeMove, targetFolder);
        verify(_mockTable, times(1)).setValueAt(fileAfterMove, 1, 0);
        verify(_mockInfo, times(1)).setStatus(ScanStatus.PENDING);
        verify(_mockInfo, times(1)).setResults(null);
        verify(_mockTable, times(1)).setValueAt(_mockInfo, 1, 1);
        verify(_mockTable, times(1)).setValueAt(_mockInfo, 1, 2);
    }

}
