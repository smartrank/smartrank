package nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchJobInfo;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.ScanStatus;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;

@RunWith(MockitoJUnitRunner.class)
public class ButtonEnablingListSelectionListenerTest {

    @Mock
    JTable _mockTable;
    @Mock
    JButton _mockUpButton;
    @Mock
    JButton _mockDownButton;
    @Mock
    JButton _mockTopButton;
    @Mock
    JButton _mockBottomButton;
    @Mock
    JButton _mockRestartButton;
    @Mock
    JButton _mockCancelButton;

    @Mock
    BatchJobInfo _mockInfo;
    @Mock
    SearchCriteriaReader _mockReader;

    @Before
    public void setup() {
    }

    @Test
    public final void testNoneSelected() {
        final ButtonEnablingListSelectionListener listener = new ButtonEnablingListSelectionListener(_mockTable, _mockUpButton, _mockDownButton, _mockTopButton, _mockBottomButton, _mockRestartButton, _mockCancelButton);
        when(_mockTable.getSelectedRow()).thenReturn(-1);

        final ListSelectionEvent event = mock(ListSelectionEvent.class);
        when(event.getValueIsAdjusting()).thenReturn(false);

        listener.valueChanged(event);
        verify(_mockUpButton).setEnabled(false);
        verify(_mockDownButton).setEnabled(false);
        verify(_mockTopButton).setEnabled(false);
        verify(_mockBottomButton).setEnabled(false);
        verify(_mockRestartButton).setEnabled(false);
        verify(_mockCancelButton).setEnabled(false);
    }

    @Test
    public final void testFirstSelectedStatusPending() {
        final ButtonEnablingListSelectionListener listener = new ButtonEnablingListSelectionListener(_mockTable, _mockUpButton, _mockDownButton, _mockTopButton, _mockBottomButton, _mockRestartButton, _mockCancelButton);
        when(_mockTable.getSelectedRow()).thenReturn(0);
        when(_mockTable.getRowCount()).thenReturn(2);

        final ListSelectionEvent event = mock(ListSelectionEvent.class);
        when(event.getValueIsAdjusting()).thenReturn(false);
        when(_mockTable.getValueAt(anyInt(), anyInt())).thenReturn(_mockInfo);
        when(_mockInfo.getReader()).thenReturn(_mockReader);
        when(_mockInfo.getStatus()).thenReturn(ScanStatus.PENDING);

        listener.valueChanged(event);
        verify(_mockUpButton).setEnabled(false);
        verify(_mockDownButton).setEnabled(true);
        verify(_mockTopButton).setEnabled(false);
        verify(_mockBottomButton).setEnabled(true);
        verify(_mockRestartButton).setEnabled(false);
        verify(_mockCancelButton).setEnabled(true);
    }

    @Test
    public final void testValueAdjusting() {
        final ButtonEnablingListSelectionListener listener = new ButtonEnablingListSelectionListener(_mockTable, _mockUpButton, _mockDownButton, _mockTopButton, _mockBottomButton, _mockRestartButton, _mockCancelButton);
        final ListSelectionEvent event = mock(ListSelectionEvent.class);
        when(event.getValueIsAdjusting()).thenReturn(true);

        listener.valueChanged(event);
        verify(_mockUpButton, never()).setEnabled(anyBoolean());
        verify(_mockDownButton, never()).setEnabled(anyBoolean());
        verify(_mockTopButton, never()).setEnabled(anyBoolean());
        verify(_mockBottomButton, never()).setEnabled(anyBoolean());
        verify(_mockRestartButton, never()).setEnabled(anyBoolean());
        verify(_mockCancelButton, never()).setEnabled(anyBoolean());
    }

    @Test
    public final void testMiddleSelectedStatusPending() {
        final ButtonEnablingListSelectionListener listener = new ButtonEnablingListSelectionListener(_mockTable, _mockUpButton, _mockDownButton, _mockTopButton, _mockBottomButton, _mockRestartButton, _mockCancelButton);
        when(_mockTable.getSelectedRow()).thenReturn(1);
        when(_mockTable.getRowCount()).thenReturn(3);

        final ListSelectionEvent event = mock(ListSelectionEvent.class);
        when(event.getValueIsAdjusting()).thenReturn(false);
        when(_mockTable.getValueAt(anyInt(), anyInt())).thenReturn(_mockInfo);
        when(_mockInfo.getReader()).thenReturn(_mockReader);
        when(_mockInfo.getStatus()).thenReturn(ScanStatus.PENDING);

        listener.valueChanged(event);
        verify(_mockUpButton).setEnabled(true);
        verify(_mockDownButton).setEnabled(true);
        verify(_mockTopButton).setEnabled(true);
        verify(_mockBottomButton).setEnabled(true);
        verify(_mockRestartButton).setEnabled(false);
        verify(_mockCancelButton).setEnabled(true);
    }

    @Test
    public final void testLastSelectedStatusPending() {
        final ButtonEnablingListSelectionListener listener = new ButtonEnablingListSelectionListener(_mockTable, _mockUpButton, _mockDownButton, _mockTopButton, _mockBottomButton, _mockRestartButton, _mockCancelButton);
        when(_mockTable.getSelectedRow()).thenReturn(2);
        when(_mockTable.getRowCount()).thenReturn(3);

        final ListSelectionEvent event = mock(ListSelectionEvent.class);
        when(event.getValueIsAdjusting()).thenReturn(false);
        when(_mockTable.getValueAt(anyInt(), anyInt())).thenReturn(_mockInfo);
        when(_mockInfo.getReader()).thenReturn(_mockReader);
        when(_mockInfo.getStatus()).thenReturn(ScanStatus.PENDING);

        listener.valueChanged(event);
        verify(_mockUpButton).setEnabled(true);
        verify(_mockDownButton).setEnabled(false);
        verify(_mockTopButton).setEnabled(true);
        verify(_mockBottomButton).setEnabled(false);
        verify(_mockRestartButton).setEnabled(false);
        verify(_mockCancelButton).setEnabled(true);
    }

    @Test
    public final void testLastSelectedStatusFailed() {
        final ButtonEnablingListSelectionListener listener = new ButtonEnablingListSelectionListener(_mockTable, _mockUpButton, _mockDownButton, _mockTopButton, _mockBottomButton, _mockRestartButton, _mockCancelButton);
        when(_mockTable.getSelectedRow()).thenReturn(2);
        when(_mockTable.getRowCount()).thenReturn(3);

        final ListSelectionEvent event = mock(ListSelectionEvent.class);
        when(event.getValueIsAdjusting()).thenReturn(false);
        when(_mockTable.getValueAt(anyInt(), anyInt())).thenReturn(_mockInfo);
        when(_mockInfo.getReader()).thenReturn(_mockReader);
        when(_mockInfo.getStatus()).thenReturn(ScanStatus.FAILED);

        listener.valueChanged(event);
        verify(_mockUpButton).setEnabled(true);
        verify(_mockDownButton).setEnabled(false);
        verify(_mockTopButton).setEnabled(true);
        verify(_mockBottomButton).setEnabled(false);
        verify(_mockRestartButton).setEnabled(true);
        verify(_mockCancelButton).setEnabled(false);
    }

    @Test
    public final void testLastSelectedStatusFailedReaderNull() {
        final ButtonEnablingListSelectionListener listener = new ButtonEnablingListSelectionListener(_mockTable, _mockUpButton, _mockDownButton, _mockTopButton, _mockBottomButton, _mockRestartButton, _mockCancelButton);
        when(_mockTable.getSelectedRow()).thenReturn(2);
        when(_mockTable.getRowCount()).thenReturn(3);

        final ListSelectionEvent event = mock(ListSelectionEvent.class);
        when(event.getValueIsAdjusting()).thenReturn(false);
        when(_mockTable.getValueAt(anyInt(), anyInt())).thenReturn(_mockInfo);
        when(_mockInfo.getStatus()).thenReturn(ScanStatus.FAILED);

        listener.valueChanged(event);
        verify(_mockUpButton).setEnabled(true);
        verify(_mockDownButton).setEnabled(false);
        verify(_mockTopButton).setEnabled(true);
        verify(_mockBottomButton).setEnabled(false);
        verify(_mockRestartButton).setEnabled(false);
        verify(_mockCancelButton).setEnabled(false);
    }
}
