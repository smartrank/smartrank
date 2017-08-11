package nl.minvenj.nfi.smartrank.io.databases.codis;

import nl.minvenj.nfi.smartrank.analysis.ExclusionReason;
import nl.minvenj.nfi.smartrank.domain.Sample;

public class CodisRecord {

    static final CodisRecord END_OF_FILE = new CodisRecord(true);
    static final CodisRecord INVALID = new CodisRecord(false);

    private final Sample _sample;
    private boolean _endOfFile;
    private final boolean _success;
    private final ExclusionReason _exclusionReason;

    public CodisRecord(final Sample sample) {
        _sample = sample;
        _success = true;
        _exclusionReason = null;
    }

    public CodisRecord(final Sample sample, final ExclusionReason reason) {
        _sample = sample;
        _success = false;
        _exclusionReason = reason;
    }

    private CodisRecord(final boolean isEof) {
        _endOfFile = isEof;
        _sample = null;
        _success = false;
        _exclusionReason = null;
    }

    public boolean isEndOfFile() {
        return _endOfFile;
    }

    public boolean isSuccess() {
        return _success;
    }

    public ExclusionReason getExclusionReason() {
        return _exclusionReason;
    }

    public Sample getSample() {
        return _sample;
    }
}
