package nl.minvenj.nfi.smartrank.gui.tabs.batchmode;

import java.util.EnumSet;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

final class ButtonEnablingListSelectionListener implements ListSelectionListener {

    private final JTable _table;
    private final JButton _upButton;
    private final JButton _downButton;
    private final JButton _topButton;
    private final JButton _bottomButton;
    private final JButton _restartButton;
    private final JButton _cancelJobButton;

    public ButtonEnablingListSelectionListener(final JTable table, final JButton upButton, final JButton downButton, final JButton topButton, final JButton bottomButton, final JButton restartButton, final JButton cancelJobButton) {
        _table = table;
        _upButton = upButton;
        _downButton = downButton;
        _topButton = topButton;
        _bottomButton = bottomButton;
        _restartButton = restartButton;
        _cancelJobButton = cancelJobButton;
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            final int selectedRow = _table.getSelectedRow();
            _topButton.setEnabled(selectedRow > 0);
            _upButton.setEnabled(selectedRow > 0);
            _downButton.setEnabled(selectedRow >= 0 && selectedRow < _table.getRowCount() - 1);
            _bottomButton.setEnabled(selectedRow >= 0 && selectedRow < _table.getRowCount() - 1);

            boolean restartEnabled = selectedRow >= 0;
            boolean cancelEnabled = selectedRow >= 0;
            if (restartEnabled) {
                final BatchJobInfo info = (BatchJobInfo) _table.getValueAt(selectedRow, 2);
                restartEnabled = (info.getReader() != null) && !EnumSet.of(ScanStatus.PROCESSING, ScanStatus.PENDING).contains(info.getStatus());
                cancelEnabled = (info.getStatus() == ScanStatus.PENDING);
            }
            _restartButton.setEnabled(restartEnabled);
            _cancelJobButton.setEnabled(cancelEnabled);
        }
    }
}