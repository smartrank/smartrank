package nl.minvenj.nfi.smartrank.gui.tabs.batchmode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

public class ClearCompletedRowsActionListener implements ActionListener {

    private final BatchProcessingTable _filesTable;

    public ClearCompletedRowsActionListener(final BatchProcessingTable filesTable) {
        _filesTable = filesTable;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        int row = 0;
        while (row < _filesTable.getRowCount()) {
            final BatchJobInfo info = (BatchJobInfo) _filesTable.getValueAt(row, 1);
            if (EnumSet.of(ScanStatus.CANCELLED, ScanStatus.SUCCEEDED, ScanStatus.FAILED).contains(info.getStatus())) {
                _filesTable.removeRow(row);
            }
            else {
                row++;
            }
        }
    }
}
