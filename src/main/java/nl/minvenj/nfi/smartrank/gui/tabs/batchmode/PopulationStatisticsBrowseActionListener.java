package nl.minvenj.nfi.smartrank.gui.tabs.batchmode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;

final class PopulationStatisticsBrowseActionListener implements ActionListener {

    private final BatchModePanel _batchModePanel;
    private final JTextField _targetField;

    PopulationStatisticsBrowseActionListener(final BatchModePanel batchModePanel, final JTextField targetField) {
        _batchModePanel = batchModePanel;
        _targetField = targetField;

    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final JFileChooser chooser = new JFileChooser(new File(_targetField.getText()));
        chooser.setDialogTitle("Please select the default population statistics file");
        chooser.setSelectedFile(new File(_targetField.getText()));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(_batchModePanel)) {
            _targetField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
}