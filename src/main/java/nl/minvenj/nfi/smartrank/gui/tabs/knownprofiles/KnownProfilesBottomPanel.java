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

import java.util.Collection;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;
import nl.minvenj.nfi.smartrank.messages.commands.UpdateKnownProfiles;
import nl.minvenj.nfi.smartrank.messages.data.EnabledLociMessage;
import nl.minvenj.nfi.smartrank.messages.data.KnownProfilesMessage;
import nl.minvenj.nfi.smartrank.raven.NullUtils;
import nl.minvenj.nfi.smartrank.raven.annotations.ExecuteOnSwingEventThread;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.utils.OrderMergedList;

public class KnownProfilesBottomPanel extends SmartRankPanel {
    private nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable _profileOverviewTable;
    private javax.swing.JScrollPane _profileOverviewScrollPane;
    private boolean _anyProfileEnabled = false;

    /**
     * Creates new form KnownProfilesBottomPanel
     */
    public KnownProfilesBottomPanel() {
        initComponents();
        registerAsListener();
    }

    private void initComponents() {
        setLayout(new MigLayout("", "[430px,grow]", "[278px,grow]"));

        _profileOverviewScrollPane = new javax.swing.JScrollPane();
        _profileOverviewTable = new nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable();
        _profileOverviewTable.setName("knownProfileOverviewTable");
        _profileOverviewScrollPane.setViewportView(_profileOverviewTable);

        add(_profileOverviewScrollPane, "cell 0 0,grow");
    }

    @RavenMessageHandler(KnownProfilesMessage.class)
    @ExecuteOnSwingEventThread
    public void onChangeProfiles(final Collection<Sample> profiles) {
        _profileOverviewTable.setRowCount(0);
        _anyProfileEnabled = false;
        final DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        };

        // Enumerate all loci

        final List<String> enabledLoci = MessageBus.getInstance().query(EnabledLociMessage.class);
        if (NullUtils.safeSize(enabledLoci) > 0) {
            final OrderMergedList<String> loci = new OrderMergedList<>(enabledLoci);
            for (final Sample profile : profiles) {
                if (profile.isEnabled()) {
                    _anyProfileEnabled = true;
                    for (final Locus locus : profile.getLoci()) {
                        loci.add(locus.getName());
                    }
                }
            }

            if (_anyProfileEnabled) {
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
            }
        }

        _profileOverviewTable.setModel(model);
    }

    @RavenMessageHandler(UpdateKnownProfiles.class)
    @ExecuteOnSwingEventThread
    public void onProfileUpdated() {
        onChangeProfiles(MessageBus.getInstance().query(KnownProfilesMessage.class));
    }
}
