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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.SmartRank;

/**
 * A Dialog containing a JavaScript editor that can be used to edit the post-processing script.
 */
public class JavaScriptEditorDialog extends JDialog {

    private boolean _ok;
    final RSyntaxTextArea _jsEditor;
    private final Set<Class<?>> _loggedClasses = new HashSet<>();
    final JTree _infoTree;

    /**
     * Create the dialog.
     *
     * @param parent the parent component of the dialog
     * @param script the script to edit
     *
     * @throws IOException
     */
    public JavaScriptEditorDialog(final JComponent parent, final String script) {
        setBounds(new Rectangle(100, 100, 600, 400));
        setModal(true);
        setTitle("SmartRank post processing script");
        setIconImage(new ImageIcon(getClass().getResource("/images/16x16/script.png")).getImage());
        setLocationRelativeTo(parent);

        getContentPane().setLayout(new BorderLayout());

        final JPanel contentPanel = new JPanel();
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentPanel.setLayout(new MigLayout("", "[210.00,grow]", "[][grow]"));

        final JSplitPane consoleSplitPane = new JSplitPane();
        consoleSplitPane.setResizeWeight(0.8);
        contentPanel.add(consoleSplitPane, "cell 0 1,grow");
        consoleSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        final JSplitPane infoSplitPane = new JSplitPane();
        consoleSplitPane.setLeftComponent(infoSplitPane);
        infoSplitPane.setResizeWeight(0.8);

        final JScrollPane scriptScrollPane = new JScrollPane();
        infoSplitPane.setLeftComponent(scriptScrollPane);

        _jsEditor = new RSyntaxTextArea();
        scriptScrollPane.setViewportView(_jsEditor);
        _jsEditor.setText(script);
        _jsEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        LanguageSupportFactory.get().register(_jsEditor);
        _jsEditor.setCodeFoldingEnabled(true);
        _jsEditor.setAutoIndentEnabled(true);
        _jsEditor.setMarkOccurrences(true);
        _jsEditor.setParserDelay(10);

        final JScrollPane infoScrollPane = new JScrollPane();
        infoSplitPane.setRightComponent(infoScrollPane);

        _infoTree = new JTree();
        infoScrollPane.setViewportView(_infoTree);
        _infoTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Objects available in scripts")));
        _infoTree.setEnabled(true);
        _infoTree.setEditable(false);
        _infoTree.addMouseListener(new InfoTreeMouseAdapter(_infoTree, _jsEditor));

        final JScrollPane consoleScrollPane = new JScrollPane();
        consoleSplitPane.setRightComponent(consoleScrollPane);

        final JTextPane console = new JTextPane();
        console.setContentType("text/html");
        console.setEditable(false);
        consoleScrollPane.setViewportView(console);

        final JToolBar toolBar = new JToolBar();
        contentPanel.add(toolBar, "cell 0 0");

        final JButton saveButton = new JButton("Save", new ImageIcon(getClass().getResource("/images/16x16/disk.png")));
        saveButton.addActionListener(new SaveScriptActionListener(_jsEditor));
        toolBar.add(saveButton);

        final JButton loadButton = new JButton("Load", new ImageIcon(getClass().getResource("/images/16x16/folder.png")));
        loadButton.addActionListener(new LoadScriptActionListener(_jsEditor));
        toolBar.add(loadButton);

        final JButton testButton = new JButton("Test", new ImageIcon(getClass().getResource("/images/16x16/control_play_blue.png")));
        testButton.addActionListener(new TestScriptActionListener(_jsEditor, console));
        toolBar.add(testButton);

        final JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        final JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                _ok = true;
                dispose();
            }
        });
        okButton.setActionCommand("OK");
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                dispose();
            }
        });
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
    }

    /**
     * @return <code>true</code> if the dialog was closed using the 'OK' button
     */
    public boolean isOk() {
        return _ok;
    }

    /**
     * @return a String containing the edited script
     */
    public String getScript() {
        return _jsEditor.getText();
    }

    /**
     * Used to form the information panel in the dialog containing info on the helper objects injected into the
     * JS engine. The info panel is kind of a workaround because I could not find an easy way to integrate that
     * information into the standard code-completion functionality offered by the RSyntaxTextArea class.
     *
     * @param paramName the name of the parameter
     * @param description a textual description of the parameter
     * @param targetClass the class implementing the parameter (used to render help on member functions)
     * @param infoSoFar a {@link StringBuilder} to which info on the current parameter is added
     */
    public void addParameterInfo(final String paramName, final String description, final Class<?> targetClass, final DefaultMutableTreeNode parentNode) {
        final DefaultTreeModel model = (DefaultTreeModel) _infoTree.getModel();
        DefaultMutableTreeNode parent = parentNode;
        if (parent == null) {
            parent = (DefaultMutableTreeNode) model.getRoot();
        }

        DefaultMutableTreeNode curNode;
        if (paramName != null && description != null) {
            curNode = new DefaultMutableTreeNode("<html>" + paramName + " - " + description);
            parent.add(curNode);
        }
        else {
            curNode = parent;
        }

        if (_loggedClasses.contains(targetClass)) {
            curNode.add(new DefaultMutableTreeNode("<html><font size=-2>(See above)"));
            return;
        }
        _loggedClasses.add(targetClass);

        for (final Method method : targetClass.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                final StringBuilder methodDesc = new StringBuilder();
                methodDesc.append("<html><font color=red><b>").append(method.getReturnType().getSimpleName()).append("</b></font>").append(" <font color=blue>").append(method.getName()).append("</font><font color=green>(");
                boolean first = true;
                for (final Parameter parameter : method.getParameters()) {
                    if (!first) {
                        methodDesc.append(", ");
                    }
                    first = false;
                    methodDesc.append(parameter.getType().getSimpleName());
                    if (parameter.isNamePresent()) {
                        methodDesc.append(" ").append(parameter.getName());
                    }
                    if (parameter.isVarArgs()) {
                        methodDesc.append("...");
                    }
                }
                methodDesc.append(")");

                final DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode(methodDesc.toString());
                curNode.add(methodNode);
                if (method.getReturnType().getName().startsWith(SmartRank.class.getPackage().getName()) && method.getReturnType() != targetClass) {
                    addParameterInfo(null, null, method.getReturnType(), methodNode);
                }
            }
        }
        model.reload();
    }
}

