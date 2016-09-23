package nl.minvenj.nfi.smartrank.analysis;

import nl.minvenj.nfi.smartrank.domain.Sample;

public class ExcludedProfile {

    private final String _sampleName;
    private final ExclusionReason _reason;
    private final int _recordNumber;
    private final Object _additionalData;

    public ExcludedProfile(final Sample candidateSample, final ExclusionReason reason) {
        this(candidateSample.getName(), -1, reason, null);
    }

    public ExcludedProfile(final String sampleName, final int recordNumber, final ExclusionReason reason) {
        this(sampleName, recordNumber, reason, null);
    }

    public ExcludedProfile(final String sampleName, final int recordNumber, final ExclusionReason unexpectedNumberOfFields, final Object additionalData) {
        _sampleName = sampleName;
        _recordNumber = recordNumber;
        _reason = unexpectedNumberOfFields;
        _additionalData = additionalData;
    }

    public String getSampleName() {
        return _sampleName;
    }

    public ExclusionReason getReason() {
        return _reason;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ExcludedProfile && _sampleName.equals(((ExcludedProfile) obj)._sampleName);
    }

    public int getRecordNumber() {
        return _recordNumber;
    }

    public Object getAdditionalData() {
        return _additionalData;
    }
}
