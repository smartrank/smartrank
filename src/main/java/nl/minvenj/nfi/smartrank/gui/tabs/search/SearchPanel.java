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
package nl.minvenj.nfi.smartrank.gui.tabs.search;

import static nl.minvenj.nfi.smartrank.raven.ApplicationStatus.READING_SAMPLES;
import static nl.minvenj.nfi.smartrank.raven.ApplicationStatus.VERIFYING_DB;
import static nl.minvenj.nfi.smartrank.raven.ApplicationStatus.WAIT_CRIMESCENE_PROFILES;
import static nl.minvenj.nfi.smartrank.raven.ApplicationStatus.WAIT_DB;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;
import nl.minvenj.nfi.smartrank.messages.data.PopulationStatisticsFileMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;

public class SearchPanel extends SmartRankPanel {

    private static final Logger LOG = LoggerFactory.getLogger(SearchPanel.class);
    private static final EnumSet<ApplicationStatus> DISABLED_STATUS_SET = EnumSet.of(WAIT_DB, VERIFYING_DB, WAIT_CRIMESCENE_PROFILES, READING_SAMPLES);

    private nl.minvenj.nfi.smartrank.gui.tabs.search.control.SearchControlPanel _controlPanel;
    private nl.minvenj.nfi.smartrank.gui.tabs.search.hypothesis.DefenseHypothesisPanel _defenseHypothesisPanel;
    private nl.minvenj.nfi.smartrank.gui.tabs.search.results.ResultsPanel _lrRankingPanel;
    private nl.minvenj.nfi.smartrank.gui.tabs.search.parameters.ParametersPanel _parametersPanel;
    private nl.minvenj.nfi.smartrank.gui.tabs.search.hypothesis.ProsecutionHypothesisPanel _prosecutionHypothesisPanel;
    private javax.swing.JPanel _resultsPanel;

    /**
     * Creates new form AnalysisTab
     */
    public SearchPanel() {
        initComponents();
        registerAsListener();

        acceptSingleDroppedFile(PopulationStatisticsFileMessage.class);
    }

    private void initComponents() {

        _parametersPanel = new nl.minvenj.nfi.smartrank.gui.tabs.search.parameters.ParametersPanel();
        _resultsPanel = new javax.swing.JPanel();
        _controlPanel = new nl.minvenj.nfi.smartrank.gui.tabs.search.control.SearchControlPanel();
        _lrRankingPanel = new nl.minvenj.nfi.smartrank.gui.tabs.search.results.ResultsPanel();
        _prosecutionHypothesisPanel = new nl.minvenj.nfi.smartrank.gui.tabs.search.hypothesis.ProsecutionHypothesisPanel();
        _defenseHypothesisPanel = new nl.minvenj.nfi.smartrank.gui.tabs.search.hypothesis.DefenseHypothesisPanel();

        _parametersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters"));

        _resultsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Results"));
        _resultsPanel.setLayout(new MigLayout("", "[472.00,grow,fill][184px,grow 0,shrink 0]", "[grow,fill]"));
        _resultsPanel.add(_lrRankingPanel, "cell 0 0,grow");
        _resultsPanel.add(_controlPanel, "cell 1 0,grow");

        _prosecutionHypothesisPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Prosecution"));
        _defenseHypothesisPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Defense"));

        setLayout(new MigLayout("insets 2", "[49%:49%:49%][49%:49%:49%]", "[grow,fill][][grow,fill]"));
        add(_prosecutionHypothesisPanel, "cell 0 0,growx");
        add(_defenseHypothesisPanel, "cell 1 0,growx");
        add(_parametersPanel, "cell 0 1 2 1,growx");
        add(_resultsPanel, "cell 0 2 2 1,grow");
    }

    @RavenMessageHandler(ApplicationStatusMessage.class)
    public void handleApplicationStatus(final ApplicationStatus status) {
        setEnabled(!DISABLED_STATUS_SET.contains(status));
        LOG.debug("Panel is now {}abled", isEnabled() ? "en" : "dis");
    }
}
