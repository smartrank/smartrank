package nl.minvenj.nfi.smartrank.analysis;

import nl.minvenj.nfi.smartrank.domain.Sample;

public class ExcludedProfile {

    private final String _sampleName;
    private final ExclusionReason _reason;
    private final Object _additionalData;

    public ExcludedProfile(final Sample candidateSample, final ExclusionReason reason) {
        this(candidateSample.getName(), reason, null);
    }

    public ExcludedProfile(final String sampleName, final ExclusionReason reason) {
        this(sampleName, reason, null);
    }

    public ExcludedProfile(final String sampleName, final ExclusionReason reason, final Object additionalData) {
        _sampleName = sampleName;
        _reason = reason;
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

    @Override
    public int hashCode() {
        return _sampleName.hashCode();
    }

    public Object getAdditionalData() {
        return _additionalData;
    }
}
