/*
 * Copyright (C) 2016 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.gui.tabs.batchmode;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

public class OpenParentInDesktopActionListener implements ActionListener {

    private final JTextComponent _textComponent;
    private final JComponent _parent;
    private final String _description;

    public OpenParentInDesktopActionListener(final JComponent parent, final JTextComponent textComponent, final String description) {
        _parent = parent;
        _textComponent = textComponent;
        _description = description;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            Desktop.getDesktop().open(new File(_textComponent.getText()).getParentFile());
        }
        catch (final Throwable e1) {
            JOptionPane.showMessageDialog(_parent, "<html>Error " + _description + "!<br><i>" + sanitizeName(e1) + ": </i><br>" + e1.getMessage(), "SmartRank Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String sanitizeName(final Throwable e1) {
        return e1.getClass().getSimpleName().replaceAll("([a-z]+)([A-Z]+)", "$1 $2").replaceAll(" Exception", "");
    }
}
