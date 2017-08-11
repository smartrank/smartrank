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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.swing.SpinnerModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReaderFactory;
import nl.minvenj.nfi.smartrank.io.statistics.StatisticsReader;
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

    private static final String TIME_REGEX = "^([0-1]?[0-9]|2[0-3])[0-5][0-9]$";

    private final class TimeSpinnerModel implements SpinnerModel {
        int _curTime = 0;
        long _lastNext = 0;
        int _nextCount = 0;
        long _lastPrev = 0;
        int _prevCount = 0;
        private final Collection<ChangeListener> _listeners = new ArrayList<>();

        public TimeSpinnerModel(final String initial) {
            setValue(initial);
        }

        @Override
        public Object getValue() {
            return String.format("%02d:%02d", _curTime / 60, _curTime % 60);
        }

        @Override
        public void setValue(final Object value) {
            final Pattern pattern = Pattern.compile("([01][0-9]|2[0-3])\\:?([0-5][0-9])");
            final Matcher matcher = pattern.matcher(value.toString().trim());
            if (matcher.matches()) {
                _curTime = Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2));
            }
            for (final ChangeListener l : _listeners) {
                l.stateChanged(new ChangeEvent(this));
            }
        }

        @Override
        public Object getNextValue() {
            final long now = System.currentTimeMillis();
            _nextCount = (_lastNext == 0 || ((now - _lastNext) < 100)) ? _nextCount + 1 : 0;
            final int delta = (_nextCount < 15 || _curTime % 15 != 0) ? 1 : 15;
            _lastNext = now;
            _nextCount++;
            _prevCount = 0;
            final int temp = (_curTime + delta) % 1440;
            return String.format("%02d:%02d", temp / 60, temp % 60);
        }

        @Override
        public Object getPreviousValue() {
            final long now = System.currentTimeMillis();
            _nextCount = (_lastPrev == 0 || ((now - _lastPrev) < 100)) ? _prevCount + 1 : 0;
            final int delta = (_prevCount < 15 || _curTime % 15 != 0) ? 1 : 15;
            _lastPrev = now;
            _nextCount = 0;
            _prevCount++;
            int temp = (_curTime - delta);
            if (temp < 0)
                temp = 1440 + temp;
            return String.format("%02d:%02d", temp / 60, temp % 60);
        }

        @Override
        public void addChangeListener(final ChangeListener l) {
            _listeners.add(l);
        }

        @Override
        public void removeChangeListener(final ChangeListener l) {
            _listeners.remove(l);
        }
    }

    /**
     * A task that starts a new search on the EDT.
     */
    private final class SearchTask implements Runnable {
        @Override
        public void run() {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    startSearch();
                }
            });
        }
    }

    private static final EnumSet<ApplicationStatus> DISABLED_STATUS_SET = EnumSet.of(WAIT_DB, VERIFYING_DB);
    private static final Logger LOG = LoggerFactory.getLogger(SmartRankPanel.class);
    private static final Logger BATCHLOG = LoggerFactory.getLogger("BatchLogger");

    private final JTextField _inputFolderField;
    private final JLabel _inputFolderLabel;
    final BatchProcessingTable _filesTable;

    private final MessageBus _messageBus;
    private final AtomicBoolean _running;

    private File _currentFile;
    private File _inputFolder;
    private File _processingFolder;
    private File _failedFolder;
    private File _succeededFolder;
    private final JButton _inputFolderBrowseButton;
    private final JButton _clearCompletedButton;
    private final JButton _runButton;
    private final JButton _stopButton;
    private final JButton _moveUpButton;
    private final JButton _moveTopButton;
    private final JButton _moveDownButton;
    private final JButton _moveBottomButton;
    private final JButton _restartJobButton;
    private final JLabel _popStatsLabel;
    private final JTextField _popStatsFilenameField;
    private final JButton _popStatsBrowseButton;
    private final JLabel _statisticsErrorLabel;
    private final JButton _cancelJobButton;
    private final JSpinner _fromTime;
    private final JLabel _andLabel;
    private final JSpinner _toTime;
    private boolean _waitingMessageLogged;
    private final Component _horizontalStrut;
    private final ScheduledExecutorService _scheduler;

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
        _inputFolderField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(final DocumentEvent e) {
                doUpdate();
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                doUpdate();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                doUpdate();
            }

            private void doUpdate() {
                synchronized (_messageBus) {
                    if (_filesTable != null) {
                        _filesTable.setRowCount(0);
                    }
                    _inputFolder = new File(_inputFolderField.getText());
                    if (_inputFolder.exists() && _inputFolder.isDirectory()) {
                        SmartRankGUISettings.setLastSelectedSearchCriteriaPath(_inputFolder.getAbsolutePath());
                    }

                    _processingFolder = new File(_inputFolder, "processing");
                    _failedFolder = new File(_inputFolder, "failed");
                    _succeededFolder = new File(_inputFolder, "succeeded");
                }
            }
        });
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
        _popStatsFilenameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(final DocumentEvent e) {
                doUpdate();
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                doUpdate();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                doUpdate();
            }

            private void doUpdate() {
                boolean enableRun = true;
                _statisticsErrorLabel.setText("");
                _statisticsErrorLabel.setVisible(false);
                if (!_popStatsFilenameField.getText().isEmpty()) {
                    try {
                        new StatisticsReader(new File(_popStatsFilenameField.getText())).getStatistics();
                        SmartRankGUISettings.setLastSelectedStatisticsFileName(_popStatsFilenameField.getText());
                    }
                    catch (final Throwable t) {
                        String msg = t.getClass().getSimpleName().replaceAll("([a-z])([A-Z])", "$1 $2").replaceAll(" Exception", "");
                        if (t.getLocalizedMessage() != null && !t.getLocalizedMessage().isEmpty()) {
                            msg += ": " + t.getLocalizedMessage();
                        }
                        _statisticsErrorLabel.setText(msg);
                        _statisticsErrorLabel.setVisible(true);
                        enableRun = false;
                    }
                }
                else {
                    SmartRankGUISettings.setLastSelectedStatisticsFileName(_popStatsFilenameField.getText());
                }

                if (_runButton != null) {
                    _runButton.setEnabled(enableRun);
                }
            }
        });
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
        filesTableScrollPane.setViewportView(_filesTable);

        final JToolBar toolBar = new JToolBar("toolbar");
        toolBar.setRollover(true);
        toolBar.setFloatable(false);

        _stopButton = new JButton("Stop", new ImageIcon(getClass().getResource("/images/16x16/control_stop_blue.png")));
        _stopButton.setToolTipText("Stop processing the search criteria files");
        _stopButton.setName("stop");
        _stopButton.setEnabled(false);
        _stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                _stopButton.setEnabled(false);
                _running.set(false);
            }
        });
        toolBar.add(_stopButton);

        _runButton = new JButton("Run between", new ImageIcon(getClass().getResource("/images/16x16/control_play_blue.png")));
        _runButton.setName("run");
        _runButton.setToolTipText("Start processing the search criteria files");
        // This line will trigger enablement of the run button
        _popStatsFilenameField.setText(SmartRankGUISettings.getLastSelectedStatisticsFileName());
        _runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                _running.set(true);
                updateControls();
                startSearch();
            }
        });
        toolBar.add(_runButton);

        _moveTopButton = new JButton("Top", new ImageIcon(getClass().getResource("/images/16x16/bullet_arrow_top.png")));
        _moveTopButton.setName("moveTop");
        _moveTopButton.setEnabled(false);
        _moveTopButton.setToolTipText("Move the selected row to the top of the list");
        _moveTopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int selectedRow = _filesTable.getSelectedRow();
                if (selectedRow > 0) {
                    _filesTable.moveRow(selectedRow, 0);
                }
            }
        });

        _fromTime = new JSpinner(new TimeSpinnerModel(SmartRankGUISettings.getBatchModeStartTime()));
        _fromTime.setMinimumSize(new Dimension(60, 20));
        _fromTime.setMaximumSize(new Dimension(60, 20));
        _fromTime.setPreferredSize(new Dimension(60, 20));
        _fromTime.setToolTipText("A time in 24-hour notation before which batch processing will not start");
        _fromTime.setName("fromTime");
        ((JSpinner.DefaultEditor) _fromTime.getEditor()).getTextField().setEditable(true);
        _fromTime.addVetoableChangeListener(new VetoableChangeListener() {
            @Override
            public void vetoableChange(final PropertyChangeEvent evt) throws PropertyVetoException {
                System.out.println(evt);
            }
        });
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

        _horizontalStrut = Box.createHorizontalStrut(8);
        toolBar.add(_horizontalStrut);

        final JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        toolBar.add(separator);
        toolBar.add(_moveTopButton);

        _moveUpButton = new JButton("Up", new ImageIcon(getClass().getResource("/images/16x16/bullet_arrow_up.png")));
        _moveUpButton.setName("moveUp");
        _moveUpButton.setEnabled(false);
        _moveUpButton.setToolTipText("Move the selected row up one position in the list");
        _moveUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int selectedRow = _filesTable.getSelectedRow();
                if (selectedRow > 0) {
                    _filesTable.moveRow(selectedRow, selectedRow - 1);
                }
            }
        });
        toolBar.add(_moveUpButton);

        _moveDownButton = new JButton("Down", new ImageIcon(getClass().getResource("/images/16x16/bullet_arrow_down.png")));
        _moveDownButton.setName("moveDown");
        _moveDownButton.setEnabled(false);
        _moveDownButton.setToolTipText("Move the selected row down one position in the list");
        _moveDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int selectedRow = _filesTable.getSelectedRow();
                _filesTable.moveRow(selectedRow, selectedRow + 1);
            }
        });
        toolBar.add(_moveDownButton);

        _moveBottomButton = new JButton("Bottom", new ImageIcon(getClass().getResource("/images/16x16/bullet_arrow_bottom.png")));
        _moveBottomButton.setName("moveBottom");
        _moveBottomButton.setEnabled(false);
        _moveBottomButton.setToolTipText("Move the selected row to the bottom of the list");
        _moveBottomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int selectedRow = _filesTable.getSelectedRow();
                _filesTable.moveRow(selectedRow, _filesTable.getRowCount() - 1);
            }
        });
        toolBar.add(_moveBottomButton);

        toolBar.add(new JSeparator(JSeparator.VERTICAL));

        _clearCompletedButton = new JButton("Clear completed", new ImageIcon(getClass().getResource("/images/16x16/table_row_delete.png")));
        _clearCompletedButton.setName("clearCompleted");
        _clearCompletedButton.setToolTipText("Remove completed search criteria files from the list");
        _clearCompletedButton.addActionListener(new ClearCompletedRowsActionListener(_filesTable));
        toolBar.add(_clearCompletedButton);

        toolBar.add(new JSeparator(JSeparator.VERTICAL));

        _restartJobButton = new JButton("Restart", new ImageIcon(getClass().getResource("/images/16x16/control_repeat_blue.png")));
        _restartJobButton.setName("restart");
        _restartJobButton.setEnabled(false);
        _restartJobButton.setToolTipText("Restart the selected interrupted job");
        _restartJobButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int selectedRow = _filesTable.getSelectedRow();
                final File file = (File) _filesTable.getValueAt(selectedRow, 0);
                _filesTable.setValueAt(moveFileToFolder(file, _inputFolder), selectedRow, 0);

                final BatchJobInfo jobInfo = (BatchJobInfo) _filesTable.getValueAt(selectedRow, 1);
                jobInfo.setStatus(ScanStatus.PENDING);
                jobInfo.setResults(null);

                _filesTable.setValueAt(jobInfo, selectedRow, 1);
                _filesTable.setValueAt(jobInfo, selectedRow, 2);
            }
        });
        toolBar.add(_restartJobButton);

        _cancelJobButton = new JButton("Cancel", new ImageIcon(getClass().getResource("/images/16x16/stop.png")));
        _cancelJobButton.setName("cancel");
        _cancelJobButton.setEnabled(false);
        _cancelJobButton.setToolTipText("Cancels the selected job");
        _cancelJobButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int selectedRow = _filesTable.getSelectedRow();
                final File file = (File) _filesTable.getValueAt(selectedRow, 0);
                _filesTable.setValueAt(moveFileToDatedFolder(file, _failedFolder), selectedRow, 0);

                final BatchJobInfo jobInfo = (BatchJobInfo) _filesTable.getValueAt(selectedRow, 1);
                jobInfo.setStatus(ScanStatus.CANCELLED);

                _filesTable.setValueAt(jobInfo, selectedRow, 1);
                _filesTable.setValueAt(jobInfo, selectedRow, 2);
            }
        });
        toolBar.add(_cancelJobButton);

        _filesTable.getSelectionModel().addListSelectionListener(new ButtonEnablingListSelectionListener(_filesTable, _moveUpButton, _moveDownButton, _moveTopButton, _moveBottomButton, _restartJobButton, _cancelJobButton));

        final JPanel filesPanel = new JPanel();
        filesPanel.setBorder(new TitledBorder(null, "Search Criteria Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        filesPanel.setLayout(new MigLayout("", "[452px,grow]", "[25px][427px,grow]"));
        filesPanel.add(toolBar, "cell 0 0,alignx left,aligny top");
        filesPanel.add(filesTableScrollPane, "cell 0 1,grow");
        add(filesPanel, "cell 0 4 4 1,grow");

        registerAsListener();

        startFilePolling();
    }

    protected void startFilePolling() {
        final Thread pollingThread = new Thread() {

            HashMap<File, Integer> _retryCounts = new HashMap<>();

            @Override
            public void run() {
                try {
                    while (true) {
                        synchronized (_messageBus) {
                            if (_inputFolder != null && _inputFolder.exists() && _inputFolder.isDirectory()) {
                                for (final File file : _inputFolder.listFiles(new FileFilter() {
                                    @Override
                                    public boolean accept(final File pathname) {
                                        return pathname.isFile() && pathname.getName().endsWith(".xml");
                                    }
                                })) {
                                    if (isNew(file)) {
                                        try {
                                            final SearchCriteriaReader reader = SearchCriteriaReaderFactory.getReader(file);
                                            final BatchJobInfo jobInfo = new BatchJobInfo(reader, ScanStatus.PENDING);
                                            _filesTable.addRow(new Object[]{file, jobInfo, jobInfo});
                                            _retryCounts.remove(file);
                                        }
                                        catch (final Throwable e) {
                                            if (_retryCounts.get(file) == null) {
                                                _retryCounts.put(file, new Integer(1));
                                            }
                                            else {
                                                _retryCounts.put(file, _retryCounts.get(file) + 1);
                                                LOG.info("Found unreadable file {}. Retry count={}", file, _retryCounts.get(file));
                                            }
                                            if (_retryCounts.get(file) >= 20) {
                                                LOG.info("Error loading file {}: {}", file.getName(), e.getMessage(), e);
                                                BATCHLOG.info("=====================");
                                                BATCHLOG.info("File: {}", file.getName());
                                                BATCHLOG.info("Requested by {}", getOwner(file));
                                                BATCHLOG.info("Requested at {}", getCreationTime(file));
                                                BATCHLOG.info("Result: Failed");
                                                BATCHLOG.info("Reason: {}", e.getMessage(), e);
                                                final File movedFile = moveFileToDatedFolder(file, _failedFolder);
                                                final BatchJobInfo jobInfo = new BatchJobInfo(ScanStatus.FAILED, NullUtils.getValue(e.getLocalizedMessage(), e.getClass().getSimpleName().replaceAll("([a-z])([A-Z])", "$1 $2") + " while reading the file!"));
                                                _filesTable.addRow(new Object[]{movedFile, jobInfo, jobInfo});
                                                _retryCounts.remove(file);
                                            }
                                        }
                                    }
                                }

                                // Remove any non-existent files still referenced in the table, or files processed longer than 30 days ago
                                for (int row = _filesTable.getRowCount() - 1; row >= 0; row--) {
                                    final File file = (File) _filesTable.getModel().getValueAt(row, 0);
                                    final BatchJobInfo info = (BatchJobInfo) _filesTable.getModel().getValueAt(row, 1);

                                    if (!file.exists()) {
                                        _filesTable.removeRow(row);
                                    }
                                    else {
                                        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        try {
                                            if (!EnumSet.of(ScanStatus.PENDING, ScanStatus.INTERRUPTED, ScanStatus.PROCESSING).contains(info.getStatus())) {
                                                final Calendar processedAt = Calendar.getInstance();
                                                processedAt.setTime(sdf.parse(info.getStatusTimestamp()));

                                                final Calendar twoWeeksAgo = Calendar.getInstance();
                                                twoWeeksAgo.add(Calendar.DAY_OF_MONTH, -14);

                                                if (processedAt.before(twoWeeksAgo)) {
                                                    _filesTable.removeRow(row);
                                                }
                                            }
                                        }
                                        catch (final ParseException e) {
                                            LOG.debug("Error decoding status date for {}: {}", file.getName(), info.getStatusTimestamp());
                                        }
                                    }
                                }
                            }
                        }
                        Thread.sleep(2000);
                    }
                }
                catch (final InterruptedException e) {
                }
            }

            private String getCreationTime(final File file) {
                try {
                    final BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return sdf.format(new Date(attributes.creationTime().toMillis()));
                }
                catch (final Exception e) {
                    LOG.info("Could not determine creation time of {}", file, e);
                }
                return "Could not be determined";
            }

            private String getOwner(final File file) {
                try {
                    return Files.getOwner(file.toPath(), LinkOption.NOFOLLOW_LINKS).getName();
                }
                catch (final Exception e) {
                    LOG.info("Could not determine owner of file {}", file, e);
                }
                return "Could not be determined";
            }

            private boolean isNew(final File file) {
                for (int idx = 0; idx < _filesTable.getModel().getRowCount(); idx++) {
                    if (((File) _filesTable.getModel().getValueAt(idx, 0)).getName().equals(file.getName()) && ScanStatus.PENDING == (((BatchJobInfo) _filesTable.getModel().getValueAt(idx, 1)).getStatus()))
                        return false;
                }
                return true;
            }
        };
        pollingThread.setName("InputFilePollingThread");
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    private void startSearch() {

        if (timeslotInvalid()) {
            waitAndSearchAgain();
            return;
        }

        LOG.debug("Scanning for next file");
        _currentFile = null;
        int idx;
        for (idx = 0; _currentFile == null && idx < _filesTable.getModel().getRowCount(); idx++) {
            final BatchJobInfo info = (BatchJobInfo) _filesTable.getModel().getValueAt(idx, 1);
            if (info.getStatus() == ScanStatus.PENDING) {
                info.setStatus(ScanStatus.PROCESSING);
                _filesTable.setValueAt(info, idx, 1);
                _filesTable.setValueAt(info, idx, 2);

                final File curFile = (File) _filesTable.getModel().getValueAt(idx, 0);

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
                    final SearchCriteriaReader reader = ((BatchJobInfo) _filesTable.getValueAt(idx, 2)).getReader();
                    if (reader.getPopulationStatistics() == null) {
                        msg = "File contains no statistics and no default statistics are configured!";
                    }
                }

                BATCHLOG.info("=====================");
                BATCHLOG.info("File: {}", curFile.getName());
                BATCHLOG.info("Requested by {}", info.getReader().getRequester());
                BATCHLOG.info("Requested at {}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(info.getReader().getRequestDateTime()));
                if (!msg.isEmpty()) {
                    LOG.info("Error processing file {}: {}", curFile.getName(), msg);
                    BATCHLOG.info("Result: Failed");
                    BATCHLOG.info("Reason: {}", msg);
                    _filesTable.setValueAt(moveFileToDatedFolder(curFile, _failedFolder), idx, 0);
                    info.setStatus(ScanStatus.FAILED);
                    info.setErrorMessage(msg);
                    _filesTable.setValueAt(info, idx, 1);
                    _filesTable.setValueAt(info, idx, 2);
                }
                else {
                    LOG.info("Starting next job: {}", curFile);
                    _currentFile = moveFileToFolder(curFile, _processingFolder);
                    _filesTable.getModel().setValueAt(_currentFile, idx, 0);
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
            _scheduler.schedule(new SearchTask(), 1, TimeUnit.SECONDS);
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
        if (crossingMidnight)
            mustWait = !mustWait;

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

    private void updateControls() {
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
        for (int idx = 0; idx < _filesTable.getModel().getRowCount(); idx++) {
            if (((File) _filesTable.getModel().getValueAt(idx, 0)).getName().equals(_currentFile.getName())) {
                final BatchJobInfo info = (BatchJobInfo) _filesTable.getValueAt(idx, 1);
                info.setResults(results);
                _filesTable.getModel().setValueAt(info, idx, 1);
                _filesTable.getModel().setValueAt(info, idx, 2);
                _filesTable.getModel().setValueAt(movedFile, idx, 0);
            }
        }
        searchAgain();
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

            movedFile = moveFileToFolder(_currentFile, _inputFolder);
        }
        else {
            movedFile = moveFileToDatedFolder(_currentFile, _failedFolder);
        }

        for (int idx = 0; idx < _filesTable.getModel().getRowCount(); idx++) {
            if (((File) _filesTable.getModel().getValueAt(idx, 0)).getName().equals(_currentFile.getName())) {
                final BatchJobInfo info = (BatchJobInfo) _filesTable.getValueAt(idx, 1);
                info.setResults(results);
                _filesTable.getModel().setValueAt(info, idx, 1);
                _filesTable.getModel().setValueAt(info, idx, 2);
                _filesTable.getModel().setValueAt(movedFile, idx, 0);
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

    private File moveFileToFolder(final File sourceFile, final File targetFolder) {
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

    private File moveFileToDatedFolder(final File sourceFile, final File targetFolder) {
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
}
