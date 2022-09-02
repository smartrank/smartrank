package nl.minvenj.nfi.smartrank.analysis;

public class ExcludedProfileStatistic {

    private final ExclusionReason _reason;
    private int _count;

    public ExcludedProfileStatistic(final ExclusionReason reason) {
        _reason = reason;
        _count = 0;
    }

    public ExclusionReason getReason() {
        return _reason;
    }

    public void add() {
        _count++;
    }

    public int getCount() {
        return _count;
    }

    public String getReasonDescription() {
        return _reason.getDescription();
    }
}
