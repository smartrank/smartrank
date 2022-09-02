package nl.minvenj.nfi.smartrank.analysis.parameterestimation;

public class DropoutEstimationValue {

    private final double _dropout;
    private int _count;

    public DropoutEstimationValue(final double dropout) {
        _dropout = dropout;
    }

    public double getDropout() {
        return _dropout;
    }

    public void add() {
        _count++;
    }

    public int getCount() {
        return _count;
    }

    @Override
    public String toString() {
        return "d=" + getDropout() + ", n=" + getCount();
    }
}
