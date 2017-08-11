package nl.minvenj.nfi.smartrank.gui.tabs.batchmode;

import java.text.SimpleDateFormat;
import java.util.Date;

import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;

public class BatchJobInfo {

    private final SearchCriteriaReader _reader;
    private String _errorMessage;
    private ScanStatus _status;
    private SearchResults _results;
    private String _timeStamp;

    public BatchJobInfo(final SearchCriteriaReader reader, final ScanStatus status) {
        _reader = reader;
        setStatus(status);
        setErrorMessage(null);
    }

    public BatchJobInfo(final ScanStatus status, final String errorMessage) {
        _reader = null;
        setStatus(status);
        setErrorMessage(errorMessage);
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

    public void setResults(final SearchResults results) {
        _results = results;
        _errorMessage = "";
        if (results != null) {
            if (results.isInterrupted()) {
                _status = ScanStatus.INTERRUPTED;
            }
            else {
                if (results.isSucceeded()) {
                    _status = ScanStatus.SUCCEEDED;
                }
                else {
                    _status = ScanStatus.FAILED;
                    _errorMessage = results.getFailureReason().getMessage();
                }
            }
        }
    }

    public SearchResults getResults() {
        return _results;
    }
}
