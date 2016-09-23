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
package nl.minvenj.nfi.smartrank.gui.tabs.search.results;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.SmartRank;
import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.LikelihoodRatio;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Ratio;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.samples.SampleWriter;
import nl.minvenj.nfi.smartrank.messages.data.CrimeSceneProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.utils.OrderMergedList;

public class ProfileDetailDialog extends javax.swing.JDialog {

    private final MessageBus _messageBus;
    private final Sample _profile;
    private final JLabel _profileDetailLabel;
    private final JLabel _profileNameLabel;
    private final JLabel _lociNumberLabel;
    private final JLabel _lociCountLabel;

    /**
     * Creates new form SampleDetailDialog
     * @param parent the parent component
     * @param modal <code>true</code> if the dialog is to be shown modal to the application
     * @param profile the profile to display in this dialog
     */
    public ProfileDetailDialog(final java.awt.Frame parent, final boolean modal, final LikelihoodRatio ratio) {
        super(parent, modal);

        _messageBus = MessageBus.getInstance();
        _profile = ratio.getProfile();
        
        _profileDetailLabel = new JLabel("Detail for profile:");
        _profileNameLabel = new JLabel(_profile.getName());
        _lociNumberLabel = new JLabel("Number of loci:");
        _lociCountLabel = new JLabel("" + ratio.getProfile().getLoci().size());
        
        _profileDetailLabel.setLabelFor(_profileNameLabel);
        _lociNumberLabel.setLabelFor(_lociCountLabel);

        final ZebraTable profileDetailTable = new ZebraTable();
        final JScrollPane profileDetailTableScrollPane = new javax.swing.JScrollPane();
        profileDetailTable.setName("ProfileDetailTable");
        profileDetailTableScrollPane.setViewportView(profileDetailTable);

        final JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new MigLayout("", "[89px][grow,fill][]", "[23px][]"));
        rootPanel.add(_profileDetailLabel, "cell 0 0");
        rootPanel.add(_profileNameLabel, "cell 1 0,growx");

        final JButton exportProfileButton = new JButton("Export Profile");
        exportProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                onExportProfile();
            }
        });

        exportProfileButton.setName("exportProfileButton");
        exportProfileButton.setToolTipText("Exports this profile to a CSV file");
        rootPanel.add(exportProfileButton, "cell 2 0,alignx right,aligny top");
        rootPanel.add(_lociNumberLabel, "cell 0 1");
        rootPanel.add(_lociCountLabel, "cell 1 1,growx");

        // Compose column names
        final ArrayList<String> columnNames = new ArrayList<>();
        final OrderMergedList<String> compoundLocusNames = new OrderMergedList<>();
        columnNames.add("Locus");
        for (final Sample sample : _messageBus.query(CrimeSceneProfilesMessage.class)) {
            if (sample.isEnabled()) {
                columnNames.add(sample.getName());
                for (final Locus locus : sample.getLoci()) {
                    compoundLocusNames.add(locus.getName());
                }
            }
        }
        columnNames.add(_profile.getName());
        columnNames.add("LR");

        for (final Locus locus : _profile.getLoci()) {
            compoundLocusNames.add(locus.getName());
        }

        final DefaultTableModel model = new DefaultTableModel(columnNames.toArray(), 0) {
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        };

        profileDetailTable.setModel(model);

        // Fill the table with rows
        for (final String locusName : compoundLocusNames) {
            final ArrayList<String> rowData = new ArrayList<>();
            rowData.add(locusName);
            for (final Sample sample : _messageBus.query(CrimeSceneProfilesMessage.class)) {
                if (sample.isEnabled()) {
                    rowData.add(getSafeLocusAlleles(sample, locusName, Arrays.asList(_profile)));
                }
            }

            rowData.add(getSafeLocusAlleles(_profile, locusName, _messageBus.query(CrimeSceneProfilesMessage.class)));
            final Ratio locusRatio = ratio.getRatio(locusName);
            if (locusRatio != null)
                rowData.add(locusRatio.toString());
            else
                rowData.add("");

            profileDetailTable.addRow(rowData.toArray());
        }

        // Create an overall ratio row
        final ArrayList<String> overallRow = new ArrayList<String>();
        overallRow.add("<html><b><i>Overall");
        for (final Sample sample : _messageBus.query(CrimeSceneProfilesMessage.class)) {
            if (sample.isEnabled()) {
                overallRow.add("");
            }
        }
        overallRow.add("");
        overallRow.add("<html><b><i>" + ratio.getOverallRatio().toString());
        profileDetailTable.addRow(overallRow.toArray());

        getContentPane().setLayout(new MigLayout("", "[389px,grow,fill]", "[55px][404px,grow,fill]"));
        getContentPane().add(profileDetailTableScrollPane, "cell 0 1,grow");
        getContentPane().add(rootPanel, "cell 0 0,growx,aligny top");

        setTitle("SmartRank " + SmartRank.getVersion() + " Profile details for " + ratio.getProfile().getName());
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pack();
    }

    private String getSafeLocusAlleles(final Sample sample, final String locusName, final List<Sample> referenceSamples) {
        final Locus locus = sample.getLocus(locusName);
        if (locus != null) {
            final StringBuilder builder = new StringBuilder("<html>");

            // Compare each allele in the target sample with the alleles in the references
            for (final Allele allele : locus.getAlleles()) {
                boolean isPresent = false;
                for (final Sample reference : referenceSamples) {
                    if (reference.isEnabled()) {
                        final Locus refLocus = reference.getLocus(locusName);
                        isPresent |= refLocus != null && refLocus.hasAllele(allele.getAllele());
                    }
                }
                if (!isPresent) {
                    builder.append("<span style=\"color:white; background-color: red; font-weight: bold;\">");
                }
                builder.append("&nbsp;").append(allele.getAllele()).append("&nbsp;");
                if (!isPresent) {
                    builder.append("</span>");
                }
            }

            return builder.toString();
        }
        return "";
    }

    private void onExportProfile() {
        final JFileChooser chooser = new JFileChooser(".");
        chooser.setSelectedFile(new File(_profile.getName() + ".csv"));
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(this)) {
            final File f = chooser.getSelectedFile();
            final SampleWriter writer = new SampleWriter(f);
            try {
                writer.write(_profile);
            }
            catch (final IOException e) {
                _messageBus.send(this, new ErrorStringMessage(e.getMessage()));
            }
        }
    }
}
