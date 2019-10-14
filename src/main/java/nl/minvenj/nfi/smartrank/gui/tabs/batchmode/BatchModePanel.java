/*
 * Copyright (C) 2016,2017 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.gui.tabs.batchmode;

import static nl.minvenj.nfi.smartrank.raven.ApplicationStatus.VERIFYING_DB;
import static nl.minvenj.nfi.smartrank.raven.ApplicationStatus.WAIT_DB;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners.ButtonEnablingListSelectionListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners.CancelJobActionListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners.ClearCompletedRowsActionListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners.EditScriptButtonActionListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners.FolderBrowseActionListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners.MoveBottomActionListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners.MoveDownActionListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners.MoveTopActionListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners.MoveUpActionListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners.PopulationStatisticsBrowseActionListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners.RestartJobActionListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners.RunActionListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners.StopActionListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.documentlisteners.InputFolderDocumentListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.documentlisteners.PopulationStatisticsDocumentListener;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.postprocessing.ConsoleWriter;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.postprocessing.FileUtilitiesForScript;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;
import nl.minvenj.nfi.smartrank.messages.commands.StartAnalysisCommand;
import nl.minvenj.nfi.smartrank.messages.data.DropinMessage;
import nl.minvenj.nfi.smartrank.messages.data.LoadSearchCriteriaMessage;
import nl.minvenj.nfi.smartrank.messages.data.PopulationStatisticsFileMessage;
import nl.minvenj.nfi.smartrank.messages.data.ThetaMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchAbortedMessage;
import nl.minvenj.nfi.smartrank.messages.status.SearchCompletedMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.NullUtils;
import nl.minvenj.nfi.smartrank.raven.annotations.ExecuteOnSwingEventThread;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.raven.timeformat.TimeUtils;

public class BatchModePanel extends SmartRankPanel {

    private static final long serialVersionUID = 1L;

    private static final String TIME_REGEX = "^([0-1]?[0-9]|2[0-3])[0-5][0-9]$";

    private static final EnumSet<ApplicationStatus> DISABLED_STATUS_SET = EnumSet.of(WAIT_DB, VERIFYING_DB);
    static final Logger LOG = LoggerFactory.getLogger(SmartRankPanel.class);
    static final Logger BATCHLOG = LoggerFactory.getLogger("BatchLogger");

    public final JTextField _inputFolderField;
    private final JLabel _inputFolderLabel;
    private final BatchProcessingTable _filesTable;

    public final MessageBus _messageBus;
    public final AtomicBoolean _running;

    private File _currentFile;
    private File _inputFolder;
    public File _processingFolder;
    private File _failedFolder;
    public File _succeededFolder;
    private final JButton _inputFolderBrowseButton;
    private final JButton _clearCompletedButton;
    public final JButton _runButton;
    public final JButton _stopButton;
    private final JButton _moveUpButton;
    private final JButton _moveTopButton;
    private final JButton _moveDownButton;
    private final JButton _moveBottomButton;
    private final JButton _restartJobButton;
    private final JLabel _popStatsLabel;
    public final JTextField _popStatsFilenameField;
    private final JButton _popStatsBrowseButton;
    public final JLabel _statisticsErrorLabel;
    private final JButton _cancelJobButton;
    private final JSpinner _fromTime;
    private final JLabel _andLabel;
    private final JSpinner _toTime;
    private boolean _waitingMessageLogged;
    private final ScheduledExecutorService _scheduler;

    private Thread _pollingThread;

    public BatchModePanel() {
        _running = new AtomicBoolean();
        _messageBus = MessageBus.getInstance();

        _scheduler = Executors.newSingleThreadScheduledExecutor();

        setLayout(new MigLayout("", "[][grow][grow][]", "[][][][][grow][]"));

        _inputFolderLabel = new JLabel("Input Folder");
        _inputFolderLabel.setName("inputFolderLabel");
        add(_inputFolderLabel, "cell 0 0,alignx trailing");

        _inputFolderField = new JTextField();
        _inputFolderField.setName("inputFolder");
        _inputFolderField.setColumns(10);
        _inputFolderField.getDocument().addDocumentListener(new InputFolderDocumentListener(this));
        _inputFolderField.setText(SmartRankGUISettings.getLastSelectedSearchCriteriaPath());
        add(_inputFolderField, "flowx,cell 1 0 2 1,growx");

        _inputFolderBrowseButton = new JButton("Browse...", new ImageIcon(getClass().getResource("/images/16x16/folder.png")));
        _inputFolderBrowseButton.setName("inputFolderBrowse");
        _inputFolderBrowseButton.addActionListener(new FolderBrowseActionListener(this, _inputFolderField));

        add(_inputFolderBrowseButton, "cell 3 0,growx");

        _popStatsLabel = new JLabel("Population Statistics");
        add(_popStatsLabel, "cell 0 1,alignx trailing");

        _popStatsFilenameField = new JTextField();
        _popStatsFilenameField.setName("populationStatisticsFilename");
        _popStatsFilenameField.setToolTipText("This file will be used for any search criteria files that do not contain population statistics");
        _popStatsFilenameField.setColumns(10);
        _popStatsFilenameField.getDocument().addDocumentListener(new PopulationStatisticsDocumentListener(this));
        add(_popStatsFilenameField, "cell 1 1 2 1,growx");

        _popStatsBrowseButton = new JButton("Browse...", new ImageIcon(getClass().getResource("/images/16x16/folder.png")));
        _popStatsBrowseButton.setName("populationStatisticsBrowse");
        _popStatsBrowseButton.addActionListener(new PopulationStatisticsBrowseActionListener(this, _popStatsFilenameField));
        add(_popStatsBrowseButton, "cell 3 1,growx");

        _statisticsErrorLabel = new JLabel("", new ImageIcon(getClass().getResource("/images/16x16/table_error.png")), SwingConstants.LEFT);
        _statisticsErrorLabel.setFont(_statisticsErrorLabel.getFont().deriveFont(_statisticsErrorLabel.getFont().getStyle() | Font.BOLD));
        _statisticsErrorLabel.setForeground(Color.WHITE);
        _statisticsErrorLabel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        _statisticsErrorLabel.setOpaque(true);
        _statisticsErrorLabel.setBackground(Color.RED);
        _statisticsErrorLabel.setName("populationStatisticsError");
        _statisticsErrorLabel.setVisible(false);
        add(_statisticsErrorLabel, "cell 1 2 3 1,growx");

        final BatchModeDetailPanel detailPanel = new BatchModeDetailPanel();
        add(detailPanel, "cell 0 5 4 1,growx");

        _filesTable = new BatchProcessingTable("inputFilesTable", detailPanel);

        final JScrollPane filesTableScrollPane = new JScrollPane();
        filesTableScrollPane.setViewportView(getFilesTable());

        final JToolBar toolBar = new JToolBar("toolbar");
        toolBar.setRollover(true);
        toolBar.setFloatable(false);

        _stopButton = new JButton("Stop", new ImageIcon(getClass().getResource("/images/16x16/control_stop_blue.png")));
        _stopButton.setToolTipText("Stop processing the search criteria files");
        _stopButton.setName("stop");
        _stopButton.setEnabled(false);
        _stopButton.addActionListener(new StopActionListener(this));
        toolBar.add(_stopButton);

        _runButton = new JButton("Run between", new ImageIcon(getClass().getResource("/images/16x16/control_play_blue.png")));
        _runButton.setName("run");
        _runButton.setToolTipText("Start processing the search criteria files");
        // This line will trigger enablement of the run button
        _popStatsFilenameField.setText(SmartRankGUISettings.getLastSelectedStatisticsFileName());
        _runButton.addActionListener(new RunActionListener(this));
        toolBar.add(_runButton);

        _fromTime = new JSpinner(new TimeSpinnerModel(SmartRankGUISettings.getBatchModeStartTime()));
        _fromTime.setMinimumSize(new Dimension(60, 20));
        _fromTime.setMaximumSize(new Dimension(60, 20));
        _fromTime.setPreferredSize(new Dimension(60, 20));
        _fromTime.setToolTipText("A time in 24-hour notation before which batch processing will not start");
        _fromTime.setName("fromTime");
        ((JSpinner.DefaultEditor) _fromTime.getEditor()).getTextField().setEditable(true);
        toolBar.add(_fromTime);

        _andLabel = new JLabel("and");
        toolBar.add(_andLabel);

        _toTime = new JSpinner(new TimeSpinnerModel(SmartRankGUISettings.getBatchModeEndTime()));
        _toTime.setPreferredSize(new Dimension(60, 20));
        _toTime.setMaximumSize(new Dimension(60, 20));
        _toTime.setMinimumSize(new Dimension(60, 20));
        _toTime.setToolTipText("A time in 24-hour notation after which batch processing will stop");
        _toTime.setName("toTime");
        ((JSpinner.DefaultEditor) _toTime.getEditor()).getTextField().setColumns(6);
        ((JSpinner.DefaultEditor) _toTime.getEditor()).getTextField().setEditable(true);
        toolBar.add(_toTime);

        toolBar.add(Box.createHorizontalStrut(8));

        final JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        toolBar.add(separator);

        _moveTopButton = getButton("Top", "Move the selected row to the top of the list", "moveTop", "/images/16x16/bullet_arrow_top.png", false, new MoveTopActionListener(this));
        toolBar.add(_moveTopButton);

        _moveUpButton = getButton("Up", "Move the selected row up one position in the list", "moveUp", "/images/16x16/bullet_arrow_up.png", false, new MoveUpActionListener(this));
        toolBar.add(_moveUpButton);

        _moveDownButton = getButton("Down", "Move the selected row down one position in the list", "moveDown", "/images/16x16/bullet_arrow_down.png", false, new MoveDownActionListener(this));
        toolBar.add(_moveDownButton);

        _moveBottomButton = getButton("Bottom", "Move the selected row to the bottom of the list", "moveBottom", "/images/16x16/bullet_arrow_bottom.png", false, new MoveBottomActionListener(this));
        toolBar.add(_moveBottomButton);

        toolBar.add(new JSeparator(JSeparator.VERTICAL));

        _clearCompletedButton = getButton("Clear completed", "Remove completed search criteria files from the list", "clearCompleted", "/images/16x16/table_row_delete.png", true, new ClearCompletedRowsActionListener(getFilesTable()));
        toolBar.add(_clearCompletedButton);

        toolBar.add(new JSeparator(JSeparator.VERTICAL));

        _restartJobButton = getButton("Restart", "Restart the selected interrupted job", "restart", "/images/16x16/control_repeat_blue.png", false, new RestartJobActionListener(this));
        toolBar.add(_restartJobButton);

        _cancelJobButton = getButton("Cancel", "Cancels the selected job", "cancel", "/images/16x16/stop.png", false, new CancelJobActionListener(this));
        toolBar.add(_cancelJobButton);

        final JButton editScriptButton = getButton("Edit PPScript", "Edits the post processing script", "editPostProcessing", "/images/16x16/script.png", true, new EditScriptButtonActionListener(this));
        toolBar.add(editScriptButton);

        getFilesTable().getSelectionModel().addListSelectionListener(new ButtonEnablingListSelectionListener(getFilesTable(), _moveUpButton, _moveDownButton, _moveTopButton, _moveBottomButton, _restartJobButton, _cancelJobButton));

        final JPanel filesPanel = new JPanel();
        filesPanel.setBorder(new TitledBorder(null, "Search Criteria Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        filesPanel.setLayout(new MigLayout("", "[452px,grow]", "[25px][427px,grow]"));
        filesPanel.add(toolBar, "cell 0 0,alignx left,aligny top");
        filesPanel.add(filesTableScrollPane, "cell 0 1,grow");
        add(filesPanel, "cell 0 4 4 1,grow");

        registerAsListener();

        final Thread watchDog = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        startFilePolling();
                        Thread.sleep(2000);
                    }
                }
                catch (final InterruptedException e) {
                }
            }
        };
        watchDog.setDaemon(true);
        watchDog.setName("BatchModeFilePollingWatchDog");
        watchDog.start();
    }

    private JButton getButton(final String text, final String tooltip, final String name, final String iconResource, final boolean enabledByDefault, final ActionListener actionListener) {
        final JButton button = new JButton(text, new ImageIcon(getClass().getResource(iconResource)));
        button.setName(name);
        button.setEnabled(enabledByDefault);
        button.setToolTipText(tooltip);
        button.addActionListener(actionListener);
        return button;
    }

    protected void startFilePolling() {
        if (_pollingThread == null || !_pollingThread.isAlive()) {
            _pollingThread = new FilePollingThread(this);
            _pollingThread.setName("InputFilePollingThread");
            _pollingThread.setDaemon(true);
            _pollingThread.start();
        }
    }

    public void startSearch() {

        if (timeslotInvalid()) {
            waitAndSearchAgain();
            return;
        }

        LOG.debug("Scanning for next file");
        _currentFile = null;
        int idx;
        for (idx = 0; _currentFile == null && idx < getFilesTable().getModel().getRowCount(); idx++) {
            final BatchJobInfo info = (BatchJobInfo) getFilesTable().getModel().getValueAt(idx, 1);
            if (info.getStatus() == ScanStatus.PENDING) {
                info.setStatus(ScanStatus.PROCESSING);
                getFilesTable().setValueAt(info, idx, 1);
                getFilesTable().setValueAt(info, idx, 2);

                final File curFile = (File) getFilesTable().getModel().getValueAt(idx, 0);

                String msg = "";
                if (!curFile.exists()) {
                    msg = "File does not exist!";
                }
                if (msg.isEmpty() && curFile.isDirectory()) {
                    msg = "Not a file but a directory!";
                }
                if (msg.isEmpty() && !curFile.isFile()) {
                    msg = "Not a normal file!";
                }
                if (msg.isEmpty() && !curFile.canRead()) {
                    msg = "Possible acces conditions problem: file exists and is an actual file, but cannot be read!";
                }

                if (msg.isEmpty() && _popStatsFilenameField.getText().isEmpty()) {
                    final SearchCriteriaReader reader = ((BatchJobInfo) getFilesTable().getValueAt(idx, 2)).getReader();
                    if (reader.getPopulationStatistics() == null) {
                        msg = "File contains no statistics and no default statistics are configured!";
                    }
                }

                BATCHLOG.info("=====================");
                BATCHLOG.info("File: {}", curFile.getName());
                BATCHLOG.info("Requested by {}", info.getReader().getRequester());

                String requestTime;
                if (info.getReader().getRequestDateTime() != null) {
                    requestTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(info.getReader().getRequestDateTime());
                }
                else {
                    requestTime = "Unknown time";
                }
                BATCHLOG.info("Requested at {}", requestTime);
                if (!msg.isEmpty()) {
                    LOG.info("Error processing file {}: {}", curFile.getName(), msg);
                    BATCHLOG.info("Result: Failed");
                    BATCHLOG.info("Reason: {}", msg);
                    getFilesTable().setValueAt(moveFileToDatedFolder(curFile, getFailedFolder()), idx, 0);
                    info.setErrorMessage(msg);
                    info.setStatus(ScanStatus.FAILED);
                    getFilesTable().setValueAt(info, idx, 1);
                    getFilesTable().setValueAt(info, idx, 2);
                }
                else {
                    LOG.info("Starting next job: {}", curFile);
                    _currentFile = moveFileToFolder(curFile, _processingFolder);
                    getFilesTable().getModel().setValueAt(_currentFile, idx, 0);
                }
            }
        }
        if (_currentFile != null) {
            _messageBus.send(this, new DropinMessage(SmartRankRestrictions.getDropinDefault()));
            _messageBus.send(this, new ThetaMessage(SmartRankRestrictions.getThetaDefault()));
            _messageBus.send(this, new PopulationStatisticsFileMessage(new File(_popStatsFilenameField.getText())));
            _messageBus.waitIdle(5000);
            _messageBus.send(this, new LoadSearchCriteriaMessage(Arrays.asList(_currentFile)));
            _messageBus.send(this, new StartAnalysisCommand());
        }
        else {
            searchAgain();
        }
    }

    private void waitAndSearchAgain() {
        updateControls();
        if (_running.get()) {
            _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.BATCHMODE_RUNNING));
            _scheduler.schedule(new SearchTask(this), 1, TimeUnit.SECONDS);
        }
        else {
            _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.BATCHMODE_IDLE));
        }
    }

    private boolean timeslotInvalid() {
        final String currentTimeString = new SimpleDateFormat("HHmm").format(new Date());
        String fromTimeString = _fromTime.getValue().toString().trim().replaceAll("\\:", "");
        if (fromTimeString.isEmpty()) {
            fromTimeString = "00:00";
        }

        String toTimeString = _toTime.getValue().toString().trim().replaceAll("\\:", "");
        if (toTimeString.isEmpty()) {
            fromTimeString = "23:59";
        }

        final boolean fromTimeIsValidTime = fromTimeString.matches(TIME_REGEX);
        if (fromTimeIsValidTime) {
            SmartRankGUISettings.setBatchModeStartTime(_fromTime.getValue().toString());
        }

        final boolean toTimeIsValidTime = toTimeString.matches(TIME_REGEX);
        if (toTimeIsValidTime) {
            SmartRankGUISettings.setBatchModeEndTime(_toTime.getValue().toString());
        }

        final boolean fromTimeEarlier = fromTimeString.compareTo(currentTimeString) <= 0;
        final boolean toTimeLater = toTimeString.compareTo(currentTimeString) >= 0;

        boolean crossingMidnight = false;
        if (fromTimeIsValidTime && toTimeIsValidTime && fromTimeString.compareTo(toTimeString) > 0) {
            final String tmp = fromTimeString;
            fromTimeString = toTimeString;
            toTimeString = tmp;
            crossingMidnight = true;
        }

        boolean mustWait = (fromTimeIsValidTime && !fromTimeEarlier) || (toTimeIsValidTime && !toTimeLater);
        if (crossingMidnight) {
            mustWait = !mustWait;
        }

        if (mustWait) {
            if (!_waitingMessageLogged) {
                LOG.info("Batch mode is configured to run between {} and {}. Waiting...", crossingMidnight ? toTimeString : fromTimeString, crossingMidnight ? fromTimeString : toTimeString);
                _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.BATCHMODE_WAITINGTORUN));
                _waitingMessageLogged = true;
            }
        }
        else {
            _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.BATCHMODE_RUNNING));
            _waitingMessageLogged = false;
        }
        return mustWait;
    }

    private void searchAgain() {
        updateControls();
        if (_running.get()) {
            _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.BATCHMODE_RUNNING));
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    startSearch();
                }
            });
        }
        else {
            _messageBus.send(this, new ApplicationStatusMessage(ApplicationStatus.BATCHMODE_IDLE));
        }
    }

    public void updateControls() {
        _inputFolderLabel.setEnabled(!_running.get());
        _inputFolderField.setEnabled(!_running.get());
        _inputFolderBrowseButton.setEnabled(!_running.get());
        _popStatsLabel.setEnabled(!_running.get());
        _popStatsFilenameField.setEnabled(!_running.get());
        _popStatsBrowseButton.setEnabled(!_running.get());
        _runButton.setEnabled(!_running.get());
        _stopButton.setEnabled(_running.get());
        _fromTime.setEnabled(!_running.get());
        _andLabel.setEnabled(!_running.get());
        _toTime.setEnabled(!_running.get());
    }

    @RavenMessageHandler(SearchCompletedMessage.class)
    @ExecuteOnSwingEventThread
    public void onSearchCompleted(final SearchResults results) {
        final File movedFile = moveFileToDatedFolder(_currentFile, _succeededFolder);
        logResults(results);
        for (int idx = 0; idx < getFilesTable().getModel().getRowCount(); idx++) {
            if (((File) getFilesTable().getModel().getValueAt(idx, 0)).getName().equals(_currentFile.getName())) {
                final BatchJobInfo info = (BatchJobInfo) getFilesTable().getValueAt(idx, 1);
                info.setFileName(movedFile.getAbsolutePath());
                info.setResults(results);
                getFilesTable().getModel().setValueAt(info, idx, 1);
                getFilesTable().getModel().setValueAt(info, idx, 2);
                getFilesTable().getModel().setValueAt(movedFile, idx, 0);
                runPostProcessingScript(info);
            }
        }
        searchAgain();
    }

    private void runPostProcessingScript(final BatchJobInfo info) {
        if (SmartRankGUISettings.getBatchModePostProcessingScript().length() > 0) {
            LOG.info("Starting post processing script");
            final ScriptEngineManager mgr = new ScriptEngineManager();
            final ScriptEngine engine = mgr.getEngineByMimeType("application/javascript");
            engine.put("job", info);
            engine.put("log", LOG);
            engine.put("console", new ConsoleWriter());
            engine.put("FileUtils", new FileUtilitiesForScript());
            try {
                final Object retval = engine.eval(SmartRankGUISettings.getBatchModePostProcessingScript());
                LOG.info("Post processing script returned {}", retval);
            }
            catch (final Throwable se) {
                LOG.error("Error running post-processing script on {}!", info.getFileName(), se);
                info.setStatus(ScanStatus.POST_PROCESSING_SCRIPT_ERROR);
                final String msg = NullUtils.getValue(info.getErrorMessage(), "");
                if (msg.length() > 0) {
                    info.setErrorMessage("Search failed with '" + info.getErrorMessage() + "' and after this, the post processing script failed with '" + se.getMessage() + "'");
                }
                else {
                    info.setErrorMessage("The post processing script failed with '" + se.getMessage() + "'");
                }
            }
        }
        // If required, cleanup search results to avoid out of memory exceptions on large databases.
        info.clearResults();
    }

    @RavenMessageHandler(SearchAbortedMessage.class)
    @ExecuteOnSwingEventThread
    public void onSearchAborted(final SearchResults results) {
        File movedFile = null;
        logResults(results);
        if (results.isInterrupted()) {
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this, "Job aborted. Do you want to continue with the next job in the list?", "SmartRank", JOptionPane.YES_NO_OPTION)) {
                _running.set(false);
            }

            movedFile = moveFileToFolder(_currentFile, getInputFolder());
        }
        else {
            movedFile = moveFileToDatedFolder(_currentFile, getFailedFolder());
        }

        for (int idx = 0; idx < getFilesTable().getModel().getRowCount(); idx++) {
            if (((File) getFilesTable().getModel().getValueAt(idx, 0)).getName().equals(_currentFile.getName())) {
                final BatchJobInfo info = (BatchJobInfo) getFilesTable().getValueAt(idx, 1);
                info.setResults(results);
                info.setFileName(movedFile.getAbsolutePath());
                getFilesTable().getModel().setValueAt(info, idx, 1);
                getFilesTable().getModel().setValueAt(info, idx, 2);
                getFilesTable().getModel().setValueAt(movedFile, idx, 0);
                runPostProcessingScript(info);
            }
        }

        searchAgain();
    }

    private void logResults(final SearchResults results) {
        BATCHLOG.info("Duration: {}", TimeUtils.formatDuration(results.getDuration()));
        if (!NullUtils.getValue(results.getLogFileName(), "").isEmpty()) {
            BATCHLOG.info("Logfile: {}", results.getLogFileName());
        }
        if (!NullUtils.getValue(results.getReportFileName(), "").isEmpty()) {
            BATCHLOG.info("Report: {}", results.getReportFileName());
        }
        if (results.isSucceeded()) {
            BATCHLOG.info("Result: Completed successfully");
        }
        else {
            if (results.isInterrupted()) {
                BATCHLOG.info("Result: Interrupted");
            }
            else {
                BATCHLOG.info("Result: Failed");
                BATCHLOG.info("Reason: {}", results.getFailureReason() == null ? "Could not be determined" : results.getFailureReason().getMessage(), results.getFailureReason());
            }
        }
    }

    public File moveFileToFolder(final File sourceFile, final File targetFolder) {
        targetFolder.mkdirs();
        final File destinationFile = new File(targetFolder, sourceFile.getName());
        try {
            Files.move(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
            return destinationFile;
        }
        catch (final IOException e) {
            LOG.error("Error moving file!", e);
        }
        return sourceFile;
    }

    public File moveFileToDatedFolder(final File sourceFile, final File targetFolder) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final File datedFolder = new File(targetFolder, sdf.format(new Date()));
        datedFolder.mkdirs();
        final File destinationFile = new File(datedFolder, sourceFile.getName());
        try {
            Files.move(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
            return destinationFile;
        }
        catch (final IOException e) {
            LOG.error("Error moving file!", e);
        }
        return sourceFile;
    }

    @RavenMessageHandler(ApplicationStatusMessage.class)
    @ExecuteOnSwingEventThread
    public void handleApplicationStatus(final ApplicationStatus status) {
        setEnabled(_running.get() || !DISABLED_STATUS_SET.contains(status));
    }

    /**
     * Indicates if the batch mode is currently running.
     *
     * @return true if batch mode is currently in running state
     */
    public boolean isRunning() {
        return _running.get();
    }

    /**
     * Programmatically generate a click on the Run button.
     */
    public void doRun() {
        _runButton.doClick();
    }

    /**
     * @return the filesTable
     */
    public BatchProcessingTable getFilesTable() {
        return _filesTable;
    }

    /**
     * @return the failedFolder
     */
    public File getFailedFolder() {
        return _failedFolder;
    }

    /**
     * @param failedFolder the failedFolder to set
     */
    public void setFailedFolder(final File failedFolder) {
        _failedFolder = failedFolder;
    }

    /**
     * @return the inputFolder
     */
    public File getInputFolder() {
        return _inputFolder;
    }

    /**
     * @param inputFolder the inputFolder to set
     */
    public void setInputFolder(final File inputFolder) {
        _inputFolder = inputFolder;
    }
}
