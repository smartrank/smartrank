/*
 * Copyright (C) 2016 Netherlands Forensic Institute
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

import java.awt.EventQueue;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReaderFactory;
import nl.minvenj.nfi.smartrank.raven.NullUtils;

/**
 * A Thread that polls for files in the configured input folder.
 */
public final class FilePollingThread extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(FilePollingThread.class);
    private final BatchModePanel _batchModePanel;
    private final HashMap<File, Integer> _retryCounts;

    /**
     * Constructor.
     *
     * @param batchModePanel the containing BatchModePanel
     */
    FilePollingThread(final BatchModePanel batchModePanel) {
        _batchModePanel = batchModePanel;
        _retryCounts = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            while (true) {
                try {
                    synchronized (_batchModePanel._messageBus) {
                        if (_batchModePanel.getInputFolder() != null && _batchModePanel.getInputFolder().exists() && _batchModePanel.getInputFolder().isDirectory()) {
                            for (final File file : _batchModePanel.getInputFolder().listFiles((FileFilter) pathname -> pathname.isFile() && (pathname.getName().toLowerCase().endsWith(".xml") || pathname.getName().toLowerCase().endsWith(".json")))) {
                                if (isNew(file)) {
                                    processFile(file);
                                }
                            }
                            cleanupFileList();
                        }
                    }
                    Thread.sleep(2000);
                }
                catch (final InterruptedException ie) {
                    // InterruptedExceptions are thrown to the outer level to abort the thread
                    throw ie;
                }
                catch (final Throwable t) {
                    // Anything throwable that is not an InterruptedException is logged and discarded
                    LOG.warn("Error polling for new files", t);
                }
            }
        }
        catch (final InterruptedException e) {
        }
        catch (final Throwable t) {
            LOG.error("File polling thread has stopped!", t);
        }
    }

    private void cleanupFileList() {
        // Remove any non-existent files still referenced in the table, or files processed longer than 30 days ago
        for (int row = _batchModePanel.getFilesTable().getRowCount() - 1; row >= 0; row--) {
            final File file = (File) _batchModePanel.getFilesTable().getModel().getValueAt(row, 0);
            final BatchJobInfo info = (BatchJobInfo) _batchModePanel.getFilesTable().getModel().getValueAt(row, 1);

            if (!file.exists()) {
                if (info.getStatus() != ScanStatus.REMOVED) {
                    info.setStatus(ScanStatus.REMOVED);
                    info.setErrorMessage("This file was detected by SmartRank, but was removed before it could be processed.");
                    LOG.warn("File {} could not be found!", file);
                }
                continue;
            }
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                if (!EnumSet.of(ScanStatus.PENDING, ScanStatus.INTERRUPTED, ScanStatus.PROCESSING).contains(info.getStatus())) {
                    final Calendar processedAt = Calendar.getInstance();
                    processedAt.setTime(sdf.parse(info.getStatusTimestamp()));

                    final Calendar deleteJobsBeforeThisTime = Calendar.getInstance();
                    deleteJobsBeforeThisTime.add(Calendar.DAY_OF_MONTH, -SmartRankRestrictions.getBatchJobRetentionDays());

                    if (processedAt.before(deleteJobsBeforeThisTime)) {
                        _batchModePanel.getFilesTable().removeRow(row);
                    }
                }
            }
            catch (final ParseException e) {
                BatchModePanel.LOG.debug("Error decoding status date for {}: {}", file.getName(), info.getStatusTimestamp());
            }
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                _batchModePanel.getFilesTable().updateUI();
            }
        });
    }

    private void processFile(final File file) {
        try {
            final SearchCriteriaReader reader = SearchCriteriaReaderFactory.getReader(file);
            final BatchJobInfo jobInfo = new BatchJobInfo(file, reader, ScanStatus.PENDING);
            _batchModePanel.getFilesTable().addRow(new Object[]{file, jobInfo, jobInfo});
            _retryCounts.remove(file);
        }
        catch (final Throwable e) {
            if (_retryCounts.get(file) == null) {
                _retryCounts.put(file, new Integer(1));
            }
            else {
                _retryCounts.put(file, _retryCounts.get(file) + 1);
                BatchModePanel.LOG.info("Found unreadable file {}. Retry count={}", file, _retryCounts.get(file));
            }
            if (_retryCounts.get(file) >= 20) {
                BatchModePanel.LOG.info("Error loading file {}: {}", file.getName(), e.getMessage(), e);
                BatchModePanel.BATCHLOG.info("=====================");
                BatchModePanel.BATCHLOG.info("File: {}", file.getName());
                BatchModePanel.BATCHLOG.info("Requested by {}", getOwner(file));
                BatchModePanel.BATCHLOG.info("Requested at {}", getCreationTime(file));
                BatchModePanel.BATCHLOG.info("Result: Failed");
                BatchModePanel.BATCHLOG.info("Reason: {}", e.getMessage(), e);
                final File movedFile = _batchModePanel.moveFileToDatedFolder(file, _batchModePanel.getFailedFolder());
                final BatchJobInfo jobInfo = new BatchJobInfo(movedFile, ScanStatus.FAILED, NullUtils.getValue(e.getLocalizedMessage(), e.getClass().getSimpleName().replaceAll("([a-z])([A-Z])", "$1 $2") + " while reading the file!"));
                _batchModePanel.getFilesTable().addRow(new Object[]{movedFile, jobInfo, jobInfo});
                _retryCounts.remove(file);
            }
        }
    }

    private String getCreationTime(final File file) {
        try {
            final BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(new Date(attributes.creationTime().toMillis()));
        }
        catch (final Exception e) {
            BatchModePanel.LOG.info("Could not determine creation time of {}", file, e);
        }
        return "Could not be determined";
    }

    private String getOwner(final File file) {
        try {
            return Files.getOwner(file.toPath(), LinkOption.NOFOLLOW_LINKS).getName();
        }
        catch (final Exception e) {
            BatchModePanel.LOG.info("Could not determine owner of file {}", file, e);
        }
        return "Could not be determined";
    }

    private boolean isNew(final File file) {
        for (int idx = 0; idx < _batchModePanel.getFilesTable().getModel().getRowCount(); idx++) {
            if (((File) _batchModePanel.getFilesTable().getModel().getValueAt(idx, 0)).getName().equals(file.getName()) && ScanStatus.PENDING == (((BatchJobInfo) _batchModePanel.getFilesTable().getModel().getValueAt(idx, 1)).getStatus())) {
                return false;
            }
            }
        return true;
    }
}