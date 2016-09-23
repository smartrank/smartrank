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
package nl.minvenj.nfi.smartrank.gui.tabs.knownprofiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;
import nl.minvenj.nfi.smartrank.messages.commands.RemoveKnownProfiles;
import nl.minvenj.nfi.smartrank.messages.commands.UpdateKnownProfile;
import nl.minvenj.nfi.smartrank.messages.data.AddKnownFilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.KnownProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.annotations.ExecuteOnSwingEventThread;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class KnownProfilesTopPanel extends SmartRankPanel {
    private javax.swing.JButton _addButton;
    private nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable _profileTable;
    private javax.swing.JButton _removeButton;
    private javax.swing.JLabel _iconLabel;
    private javax.swing.JScrollPane _profileTableScrollPane;

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
    public KnownProfilesTopPanel() {
        initComponents();
        registerAsListener();

        _profileTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(final TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    final Sample s = (Sample) _profileTable.getModel().getValueAt(e.getFirstRow(), 1);
                    s.setEnabled((boolean) _profileTable.getModel().getValueAt(e.getFirstRow(), 0));
                    MessageBus.getInstance().send(this, new UpdateKnownProfile(s));
                }
            }
        });

        _profileTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                _removeButton.setEnabled(_profileTable.getSelectedRowCount() > 0);
            }
        });
    }

    private void initComponents() {
        setLayout(new MigLayout("", "[146px][280px,grow][]", "[29.00px][76.00px]"));
        _iconLabel = new javax.swing.JLabel();
        _iconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/128x128/eppendorf-color-closed.png")));
        _profileTable = new nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable();
        _profileTable.setModel(new ProfileTableModel());
        _profileTable.setName("crimesceneProfileTable");
        _profileTableScrollPane = new javax.swing.JScrollPane();
        _profileTableScrollPane.setViewportView(_profileTable);

        add(_iconLabel, "cell 0 0 1 2,alignx center,aligny top");
        add(_profileTableScrollPane, "cell 1 0 1 2,growx,aligny center");
        _removeButton = new javax.swing.JButton();
        _removeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/user_delete.png")));
        _removeButton.setText("Remove");
        _removeButton.setEnabled(false);
        _removeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        _addButton = new javax.swing.JButton();
        _addButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/user_add.png")));
        _addButton.setText("Add");
        _addButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        add(_addButton, "cell 2 0,growx,aligny center");
        add(_removeButton, "cell 2 1,growx,aligny top");

        _iconLabel.setName("eppendorfIcon");
        _profileTable.setName("knownProfileTable");
        _addButton.setName("addKnownProfileButton");
        _removeButton.setName("removeKnownProfileButton");
    }

    private void addButtonActionPerformed(final java.awt.event.ActionEvent evt) {
        String path = SmartRankGUISettings.getLastSelectedKnownProfilePath();
        if (path.isEmpty()) {
            path = SmartRankGUISettings.getLastSelectedCrimescenePath();
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

        chooser.setDialogTitle("Select one or more files containing known profiles");
        chooser.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            final File[] selectedFiles = chooser.getSelectedFiles();
            SmartRankGUISettings.setLastSelectedKnownProfilePath(selectedFiles[0].getParent());
            MessageBus.getInstance().send(this, new AddKnownFilesMessage(Arrays.asList(selectedFiles)));
        }
    }

    private void removeButtonActionPerformed(final java.awt.event.ActionEvent evt) {
        final List<Sample> profilesToClose = new ArrayList<>();
        for (final int idx : _profileTable.getSelectedRows()) {
            profilesToClose.add((Sample) _profileTable.getModel().getValueAt(idx, 1));
        }
        MessageBus.getInstance().send(this, new RemoveKnownProfiles(profilesToClose));
    }

    @RavenMessageHandler(KnownProfilesMessage.class)
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
    }
}
