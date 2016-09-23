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
package nl.minvenj.nfi.smartrank.gui.tabs.search.hypothesis;


import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.annotations.ExecuteOnSwingEventThread;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public abstract class HypothesisPanel extends SmartRankPanel implements TableModelListener, ChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(HypothesisPanel.class);
    private Hypothesis _hypothesis;
    private boolean _ignoreNextHypothesisChange;
    private boolean _active;
    private final MessageBus _messageBus;

    private nl.minvenj.nfi.smartrank.gui.tabs.search.hypothesis.ContributorTable _contributorTable;
    private javax.swing.JLabel _unknownContributorsLabel;
    private javax.swing.JSpinner _unknownCount;
    private javax.swing.JSpinner _unknownDropout;
    private javax.swing.JLabel _unknownDropoutLabel;
    private javax.swing.JScrollPane _contributorTableScrollPane;

    /**
     * Creates new form HypothesisPanel
     *
     * @param tableName the name of the contributor table
     */
    public HypothesisPanel(final String tableName) {
        initComponents();

        _messageBus = MessageBus.getInstance();

        setLayout(new MigLayout("", "[362px,grow][4px][64px]", "[214px,grow][20px][20px]"));
        _contributorTable.setName(tableName);
        _contributorTable.getModel().addTableModelListener(this);
        _unknownCount.addChangeListener(this);
        _unknownDropout.addChangeListener(this);

        add(_contributorTableScrollPane, "cell 0 0 3 1,grow");
        add(_unknownContributorsLabel, "cell 0 1,growx,aligny center");
        add(_unknownDropoutLabel, "cell 0 2,growx,aligny center");
        add(_unknownCount, "cell 2 1,growx,aligny top");
        add(_unknownDropout, "cell 2 2,growx,aligny top");
        _unknownDropout.setVisible(!SmartRankRestrictions.isAutomaticParameterEstimationEnabled());
        _unknownDropoutLabel.setVisible(!SmartRankRestrictions.isAutomaticParameterEstimationEnabled());

        _contributorTable.setName("contributorTable");
        _unknownCount.setName("unknownCount");
        _unknownContributorsLabel.setLabelFor(_unknownCount);
        _unknownDropout.setName("unknownDropout");
        _unknownDropoutLabel.setLabelFor(_unknownDropout);

        registerAsListener();
    }

    private void initComponents() {
        _unknownCount = new javax.swing.JSpinner();
        _unknownContributorsLabel = new javax.swing.JLabel();
        _unknownDropoutLabel = new javax.swing.JLabel();
        _unknownDropout = new javax.swing.JSpinner();
        _contributorTableScrollPane = new javax.swing.JScrollPane();
        _contributorTable = new nl.minvenj.nfi.smartrank.gui.tabs.search.hypothesis.ContributorTable();

        _unknownCount.setModel(new javax.swing.SpinnerNumberModel(0, 0, SmartRankRestrictions.getMaximumUnknownCount(), 1));

        _unknownContributorsLabel.setText("Unknown contributors");

        _unknownDropoutLabel.setText("Dropout probability for unknowns");

        _unknownDropout.setModel(new javax.swing.SpinnerNumberModel(SmartRankRestrictions.getDropoutDefault(), SmartRankRestrictions.getDropoutMinimum(), SmartRankRestrictions.getDropoutMaximum(), 0.01d));
        _unknownDropout.setEnabled(false);

        _contributorTable.setModel(new javax.swing.table.DefaultTableModel(
                                                                           new Object[][]{},
                                                                           new String[]{"Contributor", "ID", "Dropout"}) {

            Class<?>[] _types = new Class[]{
                java.lang.Boolean.class, java.lang.String.class, java.lang.Float.class
            };

            boolean[] _canEdit = new boolean [] {
                true, false, false
            };

            @Override
                public Class<?> getColumnClass(final int columnIndex) {
                return _types [columnIndex];
            }

            @Override
            public boolean isCellEditable(final int rowIndex, final int columnIndex) {
                return _canEdit [columnIndex];
            }
        });
        _contributorTableScrollPane.setViewportView(_contributorTable);
    }

    protected void setHypothesis(final Hypothesis hypo) {
        if (!_ignoreNextHypothesisChange) {
            _hypothesis = hypo;
            _contributorTable.setRowCount(0);
            if (hypo instanceof DefenseHypothesis) {
                _contributorTable.addRow(new Object[]{"", "", ""});
            }
            for (final Sample sample : hypo.getSamples()) {
                _contributorTable.addRow(new Object[]{hypo.isContributor(sample), sample, hypo.getContributor(sample).getDropoutProbability()});
            }
            _unknownCount.setValue(hypo.getUnknownCount());
            _unknownDropout.setValue(hypo.getUnknownDropoutProbability());
            _unknownDropout.setEnabled(_hypothesis == null || _hypothesis.getUnknownCount() > 0);
        }
        _ignoreNextHypothesisChange = false;
    }

    @Override
    public void tableChanged(final TableModelEvent e) {
        if (!_active && e.getType() == TableModelEvent.UPDATE) {
            final Boolean enabled = (Boolean) _contributorTable.getModel().getValueAt(e.getFirstRow(), 0);
            final Sample sample = (Sample) _contributorTable.getModel().getValueAt(e.getFirstRow(), 1);
            final Double dropout = (Double) _contributorTable.getModel().getValueAt(e.getFirstRow(), 2);
            LOG.debug("Table Updated: {} {} {}", enabled, sample, dropout);
            onUpdateContributor(enabled, sample, dropout);
        }
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        _ignoreNextHypothesisChange = true;
        _unknownDropout.setEnabled(((Integer) _unknownCount.getValue()) > 0);
        onUpdateUnknowns((Integer) _unknownCount.getValue(), (Double) _unknownDropout.getValue());
    }

    @RavenMessageHandler(ApplicationStatusMessage.class)
    @ExecuteOnSwingEventThread
    public void onStatusChange(final ApplicationStatus status) {
        final AnalysisParameters parameters = _messageBus.query(AnalysisParametersMessage.class);

        _active = status.isActive();
        _contributorTable.setEnabled(!status.isActive());
        _unknownCount.setEnabled(!status.isActive());
        _unknownContributorsLabel.setEnabled(!status.isActive());

        _unknownDropoutLabel.setEnabled(!status.isActive());
        if (parameters.isAutomaticParameterEstimationToBePerformed()) {
            _unknownDropoutLabel.setText("Dropout for unknowns will be estimated");
        }
        else {
            _unknownDropoutLabel.setText("Dropout probability for unknowns");
        }

        _unknownDropout.setEnabled(!status.isActive() && (_hypothesis == null || _hypothesis.getUnknownCount() > 0) && !parameters.isAutomaticParameterEstimationToBePerformed());

        super.setEnabled(!status.isActive());
    }

    abstract public void onUpdateContributor(boolean enabled, Sample sample, double dropout);

    abstract public void onUpdateUnknowns(int count, double dropout);
}
