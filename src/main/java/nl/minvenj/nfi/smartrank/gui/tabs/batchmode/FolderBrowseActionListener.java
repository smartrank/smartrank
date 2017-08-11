package nl.minvenj.nfi.smartrank.gui.tabs.batchmode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

final class FolderBrowseActionListener implements ActionListener {

    private final BatchModePanel _batchModePanel;
    private final JTextField _targetField;

    FolderBrowseActionListener(final BatchModePanel batchModePanel, final JTextField targetField) {
        _batchModePanel = batchModePanel;
        _targetField = targetField;

    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final JFileChooser chooser = new JFileChooser(new File(_targetField.getText()));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return "Directories";
            }

            @Override
            public boolean accept(final File f) {
                return f.isDirectory();
            }
        });
        chooser.setDialogTitle("Please select the directory from which search criteria files are to be read");
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(_batchModePanel)) {
            _targetField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
}