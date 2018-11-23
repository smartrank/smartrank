/*
 * Copyright (C) 2017 Netherlands Forensic Institute
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchModePanel;

/**
 * Displays a chooser to select a directory and stores the selected name in the supplied target field.
 */
public final class FolderBrowseActionListener implements ActionListener {

    private final BatchModePanel _batchModePanel;
    private final JTextField _targetField;

    /**
     * Constructor.
     *
     * @param batchModePanel the parent panel
     * @param targetField the field that will receive the selected folder name
     */
    public FolderBrowseActionListener(final BatchModePanel batchModePanel, final JTextField targetField) {
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