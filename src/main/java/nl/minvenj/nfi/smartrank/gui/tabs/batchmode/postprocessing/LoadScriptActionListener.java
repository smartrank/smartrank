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
package nl.minvenj.nfi.smartrank.gui.tabs.batchmode.postprocessing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadScriptActionListener implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(LoadScriptActionListener.class);

    private final JTextComponent _parent;

    /**
     * Constructor.
     *
     * @param parent the object to receive the loaded script text, also serving as parent for any error dialogs
     */
    public LoadScriptActionListener(final JTextComponent parent) {
        _parent = parent;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load script from file");
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return "Javascript Files (*.js)";
            }

            @Override
            public boolean accept(final File f) {
                return f.getName().endsWith(".js");
            }
        });
        chooser.setAcceptAllFileFilterUsed(true);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(_parent)) {
            final File file = chooser.getSelectedFile();
            try {
                _parent.setText(FileUtils.readFileToString(chooser.getSelectedFile()));
                JOptionPane.showMessageDialog(_parent, "Script read successfully.", "SmartRank information", JOptionPane.INFORMATION_MESSAGE);
            }
            catch (final IOException e2) {
                LOG.error("Error reading script from file '{}'!", file.getName(), e2);
                JOptionPane.showMessageDialog(_parent, "Error reading from " + file.getName() + ": " + e2.getClass().getSimpleName() + " - " + e2.getMessage(), "Error reading from file", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
