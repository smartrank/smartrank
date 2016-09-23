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
package nl.minvenj.nfi.smartrank.gui.tabs.search.parameters;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;
import nl.minvenj.nfi.smartrank.messages.commands.EstimateDropoutMessage;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.messages.data.DropinMessage;
import nl.minvenj.nfi.smartrank.messages.data.LRThresholdMessage;
import nl.minvenj.nfi.smartrank.messages.data.PopulationStatisticsFileMessage;
import nl.minvenj.nfi.smartrank.messages.data.PopulationStatisticsMessage;
import nl.minvenj.nfi.smartrank.messages.data.RareAlleleFrequencyMessage;
import nl.minvenj.nfi.smartrank.messages.data.ReportTopMessage;
import nl.minvenj.nfi.smartrank.messages.data.ThetaMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.annotations.ExecuteOnSwingEventThread;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class ParametersPanel extends SmartRankPanel {

    private static final Logger LOG = LoggerFactory.getLogger(ParametersPanel.class);

    private final JButton _estimateDropoutButton;
    private final JTextField _populationStatisticsFileName;
    private final MessageBus _messageBus;
    private final JLabel _populationStatisticsFileNameLabel;
    private final JLabel _lrThresholdLabel;
    private final JLabel _thetaCorrectionLabel;
    private final JLabel _rareAlleleFrequencyLabel;
    private final JLabel _dropinLabel;
    private final JButton _browseButton;
    private final JSpinner _thetaSpinner;
    private final JSpinner _dropinSpinner;

    private final JSpinner _lrThresholdSpinner;

    private final JSpinner _rareAlleleFrequencySpinner;
    private final JLabel _reportTopLabel;
    private final JSpinner _reportTopSpinner;

    /**
     * Creates new form SettingsPanel
     */
    public ParametersPanel() {
        _messageBus = MessageBus.getInstance();

        setLayout(new MigLayout("", "[95px][80px:80px][pref!][pref!][pref!][pref!][pref!][60px:60px:60px][4px][64px][pref!][80px:80px:80px][4px][grow][45px]", "[23px][20px][23px]"));

        _populationStatisticsFileNameLabel = new JLabel("Population statistics");
        add(_populationStatisticsFileNameLabel, "cell 0 0,alignx left,aligny center");

        _populationStatisticsFileName = new JTextField();
        _populationStatisticsFileName.setEditable(false);
        _populationStatisticsFileName.setDropTarget(null);
        add(_populationStatisticsFileName, "cell 1 0 13 1,growx,aligny center");

        _browseButton = new JButton("...");
        _browseButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        add(_browseButton, "cell 14 0,alignx left,aligny top");

        _lrThresholdLabel = new JLabel("LR Threshold");
        add(_lrThresholdLabel, "cell 0 1,alignx left,aligny center");

        _lrThresholdSpinner = new JSpinner(new SpinnerNumberModel(SmartRankRestrictions.getDefaultLRThreshold(), Integer.valueOf(0), null, Integer.valueOf(100)));
        connectInteger(_lrThresholdSpinner, LRThresholdMessage.class);
        add(_lrThresholdSpinner, "cell 1 1,growx,aligny top");

        _reportTopLabel = new JLabel("Report top");
        add(_reportTopLabel, "cell 3 1");

        _reportTopSpinner = new JSpinner();
        _reportTopSpinner.setModel(new SpinnerNumberModel(SmartRankRestrictions.getMaximumStoredResults(), 1, SmartRankRestrictions.getMaximumStoredResults(), 1));
        connectInteger(_reportTopSpinner, ReportTopMessage.class);
        add(_reportTopSpinner, "cell 4 1");

        _thetaCorrectionLabel = new JLabel("Theta correction");
        add(_thetaCorrectionLabel, "cell 6 1,alignx left,aligny center");

        _thetaSpinner = new JSpinner(new SpinnerNumberModel(SmartRankRestrictions.getThetaDefault(), SmartRankRestrictions.getThetaMinimum(), SmartRankRestrictions.getThetaMaximum(), 0.01d));
        connectDouble(_thetaSpinner, ThetaMessage.class);
        add(_thetaSpinner, "cell 7 1,growx,aligny top");

        _rareAlleleFrequencyLabel = new JLabel("Rare Allele Frequency");
        add(_rareAlleleFrequencyLabel, "cell 10 1,alignx left,aligny center");

        _rareAlleleFrequencySpinner = new JSpinner(new SpinnerNumberModel(0.0003d, 0.0001d, 0.01000d, 0.0001d));
        // Need to do this next bit because the standard spinner cannot handle 4 decimals...
        _rareAlleleFrequencySpinner.setEditor(new JSpinner.NumberEditor(_rareAlleleFrequencySpinner, "0.0000"));
        connectDouble(_rareAlleleFrequencySpinner, RareAlleleFrequencyMessage.class);
        add(_rareAlleleFrequencySpinner, "cell 11 1,growx,aligny top");

        _dropinLabel = new JLabel("Drop-in Probability");
        _dropinLabel.setVisible(!SmartRankRestrictions.isAutomaticParameterEstimationEnabled());
        add(_dropinLabel, "cell 0 2,alignx left,aligny center");

        _dropinSpinner = new JSpinner(new SpinnerNumberModel(SmartRankRestrictions.getDropinDefault(), SmartRankRestrictions.getDropinMinimum(), SmartRankRestrictions.getDropinMaximum(), 0.01d));
        _dropinSpinner.setVisible(!SmartRankRestrictions.isAutomaticParameterEstimationEnabled());
        connectDouble(_dropinSpinner, DropinMessage.class);
        add(_dropinSpinner, "cell 1 2,growx,aligny center");

        _estimateDropoutButton = new JButton("Estimate Dropout Probabilities");
        _estimateDropoutButton.setVisible(true);
        _estimateDropoutButton.setEnabled(false);
        _estimateDropoutButton.setName("estimateDropoutButton");
        _estimateDropoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                _messageBus.send(this, new EstimateDropoutMessage());
            }
        });
        add(_estimateDropoutButton, "cell 3 2 9 1,growx,aligny top");

        _populationStatisticsFileName.setName("populationStatisticsFileName");
        _populationStatisticsFileNameLabel.setLabelFor(_populationStatisticsFileName);
        _lrThresholdSpinner.setName("lrThreshold");
        _lrThresholdLabel.setLabelFor(_lrThresholdSpinner);
        _thetaSpinner.setName("thetaCorrection");
        _thetaCorrectionLabel.setLabelFor(_thetaSpinner);
        _rareAlleleFrequencySpinner.setName("rareAlleleFrequency");
        _rareAlleleFrequencyLabel.setLabelFor(_rareAlleleFrequencySpinner);
        _dropinSpinner.setName("dropinProbability");
        _dropinLabel.setLabelFor(_dropinSpinner);
        _browseButton.setName("browseButton");

        registerAsListener();
    }

    private void connectInteger(final JSpinner target, final Class<? extends RavenMessage<Integer>> msgClass) {
        try {
            final Constructor<? extends RavenMessage<Integer>> constructor = msgClass.getConstructor(Integer.class);
            _messageBus.send(this, constructor.newInstance(target.getValue()));
            target.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent e) {
                    try {
                        _messageBus.send(this, constructor.newInstance(target.getValue()));
                    }
                    catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
                        LOG.error("StateChanged failed!", e1);
                    }
                }
            });
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
            LOG.error("connectInteger failed!", e1);
            throw new IllegalArgumentException(e1);
        }
    }

    private void connectDouble(final JSpinner target, final Class<? extends RavenMessage<Double>> msgClass) {
        try {
            final Constructor<? extends RavenMessage<Double>> constructor = msgClass.getConstructor(Double.class);
            _messageBus.send(this, constructor.newInstance(target.getValue()));
            target.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent e) {
                    try {
                        _messageBus.send(this, constructor.newInstance(target.getValue()));
                    }
                    catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
                        LOG.error("StateChanged failed!", e1);
                    }
                }
            });
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
            LOG.error("connectInteger failed!", e1);
            throw new IllegalArgumentException(e1);
        }
    }

    private void browseButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        final JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                final String fileName = f.getName().toLowerCase();
                return f.isFile() && (fileName.endsWith(".csv") || fileName.endsWith(".txt"));
            }

            @Override
            public String getDescription() {
                return "All supported statistics files (*.txt|*.csv)";
            }
        });

        String path = SmartRankGUISettings.getLastSelectedStatisticsFileName();
        if (path.isEmpty()) {
            path = SmartRankGUISettings.getLastSelectedCrimescenePath();
        }
        chooser.setSelectedFile(new File(path));
        chooser.setDialogTitle("Select a file containing population statistics");
        chooser.setMultiSelectionEnabled(false);

        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            _messageBus.send(this, new PopulationStatisticsFileMessage(chooser.getSelectedFile()));
        }
    }

    @RavenMessageHandler(PopulationStatisticsMessage.class)
    @ExecuteOnSwingEventThread
    public void onNewPopulationStatistics(final PopulationStatistics popstats) {
        _populationStatisticsFileName.setText(popstats.getFileName());
        SmartRankGUISettings.setLastSelectedStatisticsFileName(popstats.getFileName());
    }

    @RavenMessageHandler(ApplicationStatusMessage.class)
    @ExecuteOnSwingEventThread
    public void onStatusChange(final ApplicationStatus status) {
        for(final Component c : getComponents()) {
            c.setEnabled(!status.isActive());
        }
        setEnabled(!status.isActive());

        final AnalysisParameters parameters = _messageBus.query(AnalysisParametersMessage.class);
        _thetaSpinner.setValue(_messageBus.query(ThetaMessage.class));
        _dropinSpinner.setValue(_messageBus.query(DropinMessage.class));
        _lrThresholdSpinner.setValue(_messageBus.query(LRThresholdMessage.class));
        _reportTopSpinner.setValue(parameters.getMaximumNumberOfResults());
        _rareAlleleFrequencySpinner.setValue(_messageBus.query(RareAlleleFrequencyMessage.class));

        _estimateDropoutButton.setEnabled(!status.isActive() && status != ApplicationStatus.WAIT_POPULATION_STATISTICS);
        _estimateDropoutButton.setVisible(!parameters.isAutomaticParameterEstimationToBePerformed() && SmartRankRestrictions.isManualParameterEstimationEnabled());
    }

}