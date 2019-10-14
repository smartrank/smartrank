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
package nl.minvenj.nfi.smartrank.gui.tabs.batchmode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;
import nl.minvenj.nfi.smartrank.raven.NullUtils;

public class BatchJobInfo {

    private final SearchCriteriaReader _reader;
    private String _errorMessage;
    private ScanStatus _status;
    private SearchResults _results;
    private String _timeStamp;
    private String _filename;
	private String _logFileName;
	private String _reportFileName;

    /**
     * Constructor to represent a search job that was successfully loaded (and possibly partially processed).
     *
     * @param sourceFile the file from which the search job was loaded
     * @param reader the reader representing the contents of the source file
     * @param status the current status of the search job
     */
    public BatchJobInfo(final File sourceFile, final SearchCriteriaReader reader, final ScanStatus status) {
        NullUtils.argNotNull(sourceFile, "sourceFile");
        NullUtils.argNotNull(reader, "reader");
        NullUtils.argNotNull(status, "status");
        _reader = reader;
        setFileName(sourceFile.getAbsolutePath());
        setErrorMessage(null);
        setStatus(status);
    }

    /**
     * Constructor to represent a search job that could not be loaded (e.g.  due to a corrupt file).
     *
     * @param sourceFile the file from which the search job load was attempted
     * @param status the current status of the search job
     * @param errorMessage an error message representing the reason for failure
     */
    public BatchJobInfo(final File sourceFile, final ScanStatus status, final String errorMessage) {
        NullUtils.argNotNull(sourceFile, "sourceFile");
        NullUtils.argNotNull(status, "status");
        _reader = null;
        setFileName(sourceFile.getAbsolutePath());
        setErrorMessage(errorMessage);
        setStatus(status);
    }

    /**
      * @return the reader
      */
    public SearchCriteriaReader getReader() {
        return _reader;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return _errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(final String errorMessage) {
        _errorMessage = errorMessage;
    }

    /**
     * @return the status
     */
    public ScanStatus getStatus() {
        return _status;
    }

    public String getStatusTimestamp() {
        return _timeStamp;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(final ScanStatus status) {
        _status = status;
        _timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public boolean isSucceeded() {
        return _status == ScanStatus.SUCCEEDED;
    }

    /**
     * Associates a {@link SearchResults} instance with this search job.
     *
     * @param results the {@link SearchResults} containing the results of this search job
     */
    public void setResults(final SearchResults results) {
        _errorMessage = "";
        if (results != null) {
            _logFileName = results.getLogFileName();
            _reportFileName = results.getReportFileName();
            if (results.isInterrupted()) {
                setStatus(ScanStatus.INTERRUPTED);
            }
            else {
                if (results.isSucceeded()) {
                    setStatus(ScanStatus.SUCCEEDED);
                }
                else {
                    _errorMessage = results.getFailureReason().getMessage();
                    setStatus(ScanStatus.FAILED);
                }
            }
        }
        else {
            _logFileName = null;
            _reportFileName = null;
        }
    }

    /**
     * Sets the absolute filename from which this job was loaded.
     *
     * @param absolutePath the absolute path of the file from which this search job was loaded
     */
    public void setFileName(final String absolutePath) {
        _filename = absolutePath;
    }

    /**
     * @return the absolute path and name of the file from which this search job was loaded
     */
    public String getFileName() {
        return _filename;
    }

    /**
     * @return the absolute path of the logfile generated by this job, or <code>null</code> if no logfile was generated
     */
	public String getLogFileName() {
		return _logFileName;
	}

	/**
	 * @return the absolute path of the pdf report generated by this job, or null if no report was generated.
	 */
	public String getReportFileName() {
		return _reportFileName;
	}

    /**
     * @return a {@link Properties} 
     */
    public Properties getProperties() {
        return _reader.getProperties();
    }
}
