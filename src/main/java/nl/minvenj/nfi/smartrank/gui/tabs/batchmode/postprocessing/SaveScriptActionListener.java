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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Saves the current script in response to a click on the 'Save' button.
 */
public final class SaveScriptActionListener implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(LoadScriptActionListener.class);

    private final JTextComponent _parent;

    /**
     * Constructor.
     *
     * @param parent the object containing the script text, also serving as parent for any error dialogs
     */
    public SaveScriptActionListener(final JTextComponent parent) {
        _parent = parent;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setDialogTitle("Save script to file");
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
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(_parent)) {
            final File file = chooser.getSelectedFile();
            if (!file.exists() || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(_parent, "Are you you want to overwrite '" + file.getName() + "'?", "Overwrite file?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(_parent.getText().getBytes(), 0, _parent.getText().length());
                }
                catch (final FileNotFoundException e1) {
                    LOG.error("Error writing script to file '{}'!", file.getName(), e1);
                    JOptionPane.showMessageDialog(_parent, "Cannot write to '" + file.getName() + "'!", "Error writing to file", JOptionPane.QUESTION_MESSAGE);
                }
                catch (final IOException e2) {
                    LOG.error("Error writing script to file '{}'!", file.getName(), e2);
                    JOptionPane.showMessageDialog(_parent, "There was an error writing to '" + file.getName() + "':\n" + e2.getClass().getSimpleName() + " - " + e2.getMessage(), "Error writing to file", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}