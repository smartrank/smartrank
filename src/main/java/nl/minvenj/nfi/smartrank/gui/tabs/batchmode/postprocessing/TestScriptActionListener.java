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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchJobInfo;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.ScanStatus;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReaderFactory;

/**
 * Executes a testrun of the configured script.
 */
public class TestScriptActionListener implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(TestScriptActionListener.class);

    private final JTextComponent _jsEditor;
    private final JTextPane _console;

    /**
     * Constructor.
     *
     * @param jsEditor the editor holding the script
     * @param console the component to receive console output
     */
    public TestScriptActionListener(final JTextComponent jsEditor, final JTextPane console) {
        _jsEditor = jsEditor;
        _console = console;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        _console.setText("<html>");
        final ConsoleWriter consoleWriter = new ConsoleWriter(_console);
        ScriptEngine engine = null;
        try {
            final ScriptEngineManager mgr = new ScriptEngineManager();
            engine = mgr.getEngineByMimeType("application/javascript");

            final File testFile = new File("./scriptingtestfiles/SearchCriteriaForTest.xml");
            final SearchCriteriaReader searchCriteriaReader = SearchCriteriaReaderFactory.getReader(testFile);

            final BatchJobInfo info = new BatchJobInfo(testFile, searchCriteriaReader, ScanStatus.PENDING);

            final SearchResults searchResults = new SearchResults(1000, new DatabaseConfiguration(new File("DummyDatabase"))) {
                @Override
                public List<Double> getLRs() {
                    return Arrays.asList(1000.0, 10.0, 100.0, 1000.0, 10000.0, 100000.0);
                }

                @Override
                public int getNumberOfLRs() {
                    return 6;
                }

                @Override
                public int getNumberOfLRsOver1() {
                    return 5;
                }

                @Override
                public boolean isSucceeded() {
                    return true;
                }

                @Override
                public AnalysisParameters getParameters() {
                    final AnalysisParameters parameters = new AnalysisParameters();
                    parameters.setLrThreshold(searchCriteriaReader.getLRThreshold());
                    return parameters;
                }
            };
            info.setResults(searchResults);

            engine.put("job", info);
            engine.put("log", LOG);
            engine.put("console", consoleWriter);
            engine.put("FileUtils", new FileUtilitiesForScript(consoleWriter));
        }
        catch (final Throwable t) {
            LOG.error("Post processing script init test failed", t);

            final StringWriter out = new StringWriter();
            t.printStackTrace(new PrintWriter(out));
            consoleWriter.log("<font color=red>Error setting up script engine<br><pre>" + out.toString() + "</pre></font>");
            return;
        }

        try {
            engine.getContext().setWriter(consoleWriter);
            engine.eval(_jsEditor.getText());
            consoleWriter.log("Script executed.\n");
        }
        catch (final Throwable t) {
            LOG.error("Post processing script test failed", t);
            consoleWriter.log("<font color=red>The post processing script failed with '" + t.getMessage() + "'</font><br>");
        }
    }
}
