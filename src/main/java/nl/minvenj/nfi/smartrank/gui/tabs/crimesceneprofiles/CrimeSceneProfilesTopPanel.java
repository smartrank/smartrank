/*
 * Copyright (C) 2015 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.gui.tabs.crimesceneprofiles;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;
import nl.minvenj.nfi.smartrank.messages.commands.ClearSearchCriteriaMessage;
import nl.minvenj.nfi.smartrank.messages.commands.RemoveCrimeSceneProfiles;
import nl.minvenj.nfi.smartrank.messages.commands.UpdateCrimeSceneProfile;
import nl.minvenj.nfi.smartrank.messages.data.AddCrimeSceneFilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.CrimeSceneProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.LoadSearchCriteriaMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.NullUtils;
import nl.minvenj.nfi.smartrank.raven.annotations.ExecuteOnSwingEventThread;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class CrimeSceneProfilesTopPanel extends SmartRankPanel {
    private final JButton _addButton;
    private final ZebraTable _profileTable;
    private final JButton _removeButton;
    private final JButton _loadButton;
    private final JButton _clearButton;

    /**
     * Model for the table displaying the loaded profiles
     */
    private final class ProfileTableModel extends javax.swing.table.DefaultTableModel {
        private ProfileTableModel() {
            super(null, new String[]{"Enabled", "Sample", "Filename"});
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            return columnIndex == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return columnIndex == 0;
        }
    }

    /**
     * Creates new form CrimeSceneProfilesTopPanel
     */
    public CrimeSceneProfilesTopPanel() {
        setLayout(new MigLayout("", "[128px][300px,grow][]", "[29.00px][237.00px,grow][28.00px]"));
        _profileTable = new ZebraTable();
        _profileTable.setModel(new ProfileTableModel());
        _profileTable.setName("crimeSceneProfileTable");

        _profileTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(final TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    final Sample s = (Sample) _profileTable.getModel().getValueAt(e.getFirstRow(), 1);
                    s.setEnabled((boolean) _profileTable.getModel().getValueAt(e.getFirstRow(), 0));
                    MessageBus.getInstance().send(this, new UpdateCrimeSceneProfile(s));
                }
            }
        });

        _profileTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                _removeButton.setEnabled(_profileTable.getSelectedRowCount() > 0);
            }
        });

        final JScrollPane profileTableScrollPane = new JScrollPane();
        profileTableScrollPane.setViewportView(_profileTable);
        add(profileTableScrollPane, "cell 1 0 1 2,growx,aligny center");

        final JLabel iconLabel = new JLabel();
        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/128x128/red-palmprint.png")));
        iconLabel.setName("handprintIcon");
        add(iconLabel, "cell 0 0 1 3,alignx center,aligny top");

        _addButton = new JButton();
        _addButton.setIcon(new ImageIcon(getClass().getResource("/images/16x16/user_add.png")));
        _addButton.setName("addCrimeSceneProfileButton");
        _addButton.setText("Add");
        _addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        add(_addButton, "cell 2 0,growx,aligny center");

        _removeButton = new JButton();
        _removeButton.setIcon(new ImageIcon(getClass().getResource("/images/16x16/user_delete.png")));
        _removeButton.setName("removeCrimeSceneProfileButton");
        _removeButton.setText("Remove");
        _removeButton.setEnabled(false);
        _removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        add(_removeButton, "cell 2 1 1 2,growx,aligny top");

        _loadButton = new JButton();
        _loadButton.setIcon(new ImageIcon(getClass().getResource("/images/16x16/zoom_in.png")));
        _loadButton.setText("Load Search Criteria");
        _loadButton.setName("loadSearchCriteriaButton");
        _loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                loadSearchCriteriaActionPerformed();
            }
        });

        _clearButton = new JButton();
        _clearButton.setIcon(new ImageIcon(getClass().getResource("/images/16x16/zoom_out.png")));
        _clearButton.setText("Clear Search Criteria");
        _clearButton.setName("clearSearchCriteriaButton");
        _clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                clearSearchCriteriaActionPerformed();
            }
        });

        final JPanel buttonPanel = new JPanel();
        buttonPanel.add(_loadButton);
        buttonPanel.add(_clearButton);
        add(buttonPanel, "cell 1 2,growx");
        registerAsListener();
    }

    private void addButtonActionPerformed(final ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String path = SmartRankGUISettings.getLastSelectedCrimescenePath();
        if (path.isEmpty()) {
            path = SmartRankGUISettings.getLastSelectedDatabaseFileName();
            if (!path.isEmpty()) {
                path = new File(path).getParent();
            }
        }
        final JFileChooser chooser = new JFileChooser(path);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                final String fileName = f.getName().toLowerCase();
                return f.isFile() && (fileName.endsWith(".xml") || fileName.endsWith(".csv") || fileName.endsWith(".txt"));
            }

            @Override
            public String getDescription() {
                return "All supported sample files (*.txt|*.csv|*.xml)";
            }
        });

        chooser.setDialogTitle("Select a file containing crime-scene profiles");
        chooser.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            final File[] selectedFiles = chooser.getSelectedFiles();
            SmartRankGUISettings.setLastSelectedCrimescenePath(selectedFiles[0].getParent());
            MessageBus.getInstance().send(this, new AddCrimeSceneFilesMessage(Arrays.asList(selectedFiles)));
        }
    }

    private void removeButtonActionPerformed(final java.awt.event.ActionEvent evt) {
        final List<Sample> profilesToRemove = new ArrayList<>();
        for (final int idx : _profileTable.getSelectedRows()) {
            profilesToRemove.add((Sample) _profileTable.getModel().getValueAt(idx, 1));
        }
        MessageBus.getInstance().send(this, new RemoveCrimeSceneProfiles(profilesToRemove));
    }

    private void clearSearchCriteriaActionPerformed() {
        if (NullUtils.safeSize(MessageBus.getInstance().query(CrimeSceneProfilesMessage.class)) != 0) {
            if (JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog(this, "Are you sure you want to clear the current search criteria?", "SmartRank Question", JOptionPane.OK_CANCEL_OPTION)) {
                return;
            }
        }
        MessageBus.getInstance().send(this, new ClearSearchCriteriaMessage());
    }

    private void loadSearchCriteriaActionPerformed() {
        if (NullUtils.safeSize(MessageBus.getInstance().query(CrimeSceneProfilesMessage.class)) != 0) {
            if (JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog(this, "<html>Are you sure you want to load new search criteria?<br>The current criteria will be discarded...", "SmartRank Question", JOptionPane.OK_CANCEL_OPTION)) {
                return;
            }
        }

        final JFileChooser chooser = new JFileChooser(SmartRankGUISettings.getLastSelectedSearchCriteriaPath());
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                final String fileName = f.getName().toLowerCase();
                return f.isFile() && (fileName.endsWith(".xml") || fileName.endsWith(".txt"));
            }

            @Override
            public String getDescription() {
                return "All supported sample files (*.txt|*.xml)";
            }
        });

        chooser.setDialogTitle("Select a file containing search criteria");
        chooser.setMultiSelectionEnabled(false);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            final File selectedFile = chooser.getSelectedFile();
            SmartRankGUISettings.setLastSelectedSearchCriteriaPath(selectedFile.getParent());
            MessageBus.getInstance().send(this, new LoadSearchCriteriaMessage(Arrays.asList(selectedFile)));
        }
    }

    @RavenMessageHandler(CrimeSceneProfilesMessage.class)
    @ExecuteOnSwingEventThread
    public void onProfilesChanged(final Collection<Sample> profiles) {
        _profileTable.setRowCount(0);
        for (final Sample sample : profiles) {
            _profileTable.addRow(new Object[]{sample.isEnabled(), sample, sample.getSourceFile()});
        }
        _removeButton.setEnabled(_profileTable.getSelectedRowCount() > 0);
    }

    @RavenMessageHandler(ApplicationStatusMessage.class)
    @ExecuteOnSwingEventThread
    void onStateChanged(final ApplicationStatus status) {
        _profileTable.setEnabled(!status.isActive());
        _addButton.setEnabled(!status.isActive());
        _removeButton.setEnabled(!status.isActive() && _profileTable.getSelectedRowCount() > 0);
        _loadButton.setEnabled(!status.isActive());
        _clearButton.setEnabled(!status.isActive());
    }
}
