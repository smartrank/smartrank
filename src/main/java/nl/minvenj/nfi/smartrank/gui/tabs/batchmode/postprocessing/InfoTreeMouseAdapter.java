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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 * Listens for double-clicks on any item in the infotree and copies a relevant code snippet to the editor.
 */
class InfoTreeMouseAdapter extends MouseAdapter {

    private final JTree _infoTree;
    private final RSyntaxTextArea _jsEditor;

    /**
     * Constructor.
     *
     * @param infoTree the info tree holding a description of available helper objects
     * @param jsEditor the editor to receive a relevant code snippet for the selected helper object and  method
     */
    public InfoTreeMouseAdapter(final JTree infoTree, final RSyntaxTextArea jsEditor) {
        _infoTree = infoTree;
        _jsEditor = jsEditor;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
            if (((DefaultMutableTreeNode) _infoTree.getLastSelectedPathComponent()).isLeaf()) {
                final StringBuilder sb = new StringBuilder();

                for (int cursor = 1; cursor < _infoTree.getSelectionPath().getPathCount(); cursor++) {
                    final DefaultMutableTreeNode node = (DefaultMutableTreeNode) _infoTree.getSelectionPath().getPathComponent(cursor);
                    if (node.getUserObject().toString().startsWith("<html>")) {
                        String value = node.getUserObject().toString();
                        value = value.replaceAll(" - .*", "");
                        value = value.replaceAll("<font color=red>.*?</font>", ".");
                        value = value.replaceAll("<.*?>", "");
                        value = value.replaceAll("\\s*", "");
                        sb.append(value);
                    }
                }
                _jsEditor.insert(sb.toString(), _jsEditor.getCaretPosition());
            }
        }
        super.mouseClicked(e);
    }
}