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

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;
import nl.minvenj.nfi.smartrank.messages.commands.UpdateCrimeSceneProfile;
import nl.minvenj.nfi.smartrank.messages.data.CrimeSceneProfilesMessage;
import nl.minvenj.nfi.smartrank.raven.annotations.ExecuteOnSwingEventThread;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class CrimeSceneProfilesBottomPanel extends SmartRankPanel {

    private nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable _profileOverviewTable;
    private javax.swing.JScrollPane _profileOverviewScrollPane;

    /**
     * Creates new form CrimeSceneProfilesBottomPanel
     */
    public CrimeSceneProfilesBottomPanel() {
        initComponents();
        registerAsListener();
    }

    private void initComponents() {
        setLayout(new MigLayout("", "[430px,grow]", "[278px,grow]"));

        _profileOverviewScrollPane = new javax.swing.JScrollPane();
        _profileOverviewTable = new nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable();

        _profileOverviewTable.setModel(new javax.swing.table.DefaultTableModel(
                                                                               new Object[][]{

        },
                                                                               new String[]{
                                                                                   "Locus"
                                                                               }) {
            Class<?>[] _types = new Class<?>[]{
                java.lang.String.class
            };
            boolean[] _canEdit = new boolean[]{
                false
            };

            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                return _types[columnIndex];
            }

            @Override
            public boolean isCellEditable(final int rowIndex, final int columnIndex) {
                return _canEdit[columnIndex];
            }
        });
        _profileOverviewScrollPane.setViewportView(_profileOverviewTable);
        add(_profileOverviewScrollPane, "cell 0 0,grow");

        _profileOverviewTable.setName("crimeSceneProfileOverviewTable");
    }

    @RavenMessageHandler(CrimeSceneProfilesMessage.class)
    @ExecuteOnSwingEventThread
    public void onChangeProfiles(final Collection<Sample> profiles) {
        _profileOverviewTable.setRowCount(0);
        final DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        };

        // Enumerate all loci
        final ArrayList<String> loci = new ArrayList<>();
        for (final Sample profile : profiles) {
            if (profile.isEnabled()) {
                for (final Locus locus : profile.getLoci()) {
                    if (!loci.contains(locus.getName())) {
                        loci.add(locus.getName());
                    }
                }
            }
        }

        model.addColumn("Locus", loci.toArray());

        for (final Sample profile : profiles) {
            if (profile.isEnabled()) {
                final Object[] columnData = new Object[loci.size()];
                int idx = 0;
                for (final String locusName : loci) {
                    final Locus profileLocus = profile.getLocus(locusName);
                    columnData[idx++] = profileLocus == null ? "" : profileLocus.getAlleles().toString().replaceAll("[\\[\\]]", "");
                }

                model.addColumn(profile.getName(), columnData);
            }
        }
        _profileOverviewTable.setModel(model);
    }

    @RavenMessageHandler(UpdateCrimeSceneProfile.class)
    @ExecuteOnSwingEventThread
    public void onUpdatedProfile() {
        onChangeProfiles(MessageBus.getInstance().query(CrimeSceneProfilesMessage.class));
    }
}
