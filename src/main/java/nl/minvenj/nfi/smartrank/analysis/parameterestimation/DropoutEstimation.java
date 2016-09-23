/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.smartrank.analysis.parameterestimation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Records the results of a dropout estimation.
 */
public class DropoutEstimation {

    private BigDecimal _minimum;
    private BigDecimal _maximum;

    private final HashMap<String, BigDecimal> _minimumValues = new HashMap<>();
    private final HashMap<String, BigDecimal> _maximumValues = new HashMap<>();
    private boolean _isValid;
    private int _alleleCount;
    private int _replicateCount;
    private int _iterations;
    private List<DropoutEstimationValue> _data;

    /**
     * Constructor.
     */
    public DropoutEstimation() {
        _minimum = BigDecimal.ONE;
        _maximum = BigDecimal.ZERO;
    }

    /**
     * @return <b>true</b> if the dropout estimation contains a valid result.
     */
    public boolean isValid() {
        return _isValid;
    }

    /**
     * @return the maximum value for dropout
     */
    public BigDecimal getMaximum() {
        return _maximum;
    }

    /**
     * @return the minimum value for dropout
     */
    public BigDecimal getMinimum() {
        return _minimum;
    }

    /**
     * @return the minimum value for dropout for the prosecution hypothesis
     */
    public BigDecimal getProsecutionMinimum() {
        return _minimumValues.get("Prosecution");
    }

    /**
     * @return the minimum value for dropout for the defense hypothesis
     */
    public BigDecimal getDefenseMinimum() {
        return _minimumValues.get("Defense");
    }

    /**
     * @return the maximum value for dropout for the prosecution hypothesis
     */
    public BigDecimal getProsecutionMaximum() {
        return _maximumValues.get("Prosecution");
    }

    /**
     * @return the maximum value for dropout for the defense hypothesis
     */
    public BigDecimal getDefenseMaximum() {
        return _maximumValues.get("Defense");
    }

    /**
     * Sets the allele count for the dropout estimation.
     *
     * @param alleleCount the new value for the allele count
     */
    public void setAlleleCount(final int alleleCount) {
        _alleleCount = alleleCount;
    }

    /**
     * Gets the allele count.
     *
     * @return the number of alleles for the dropout setimation
     */
    public int getAlleleCount() {
        return _alleleCount;
    }

    /**
     * Sets the number of replicates.
     *
     * @param replicateCount the number of replicates
     */
    public void setReplicateCount(final int replicateCount) {
        _replicateCount = replicateCount;
    }

    /**
     * Gets the number of replicates over which the dropout estimation has been performed.
     *
     * @return the number of replicates in the analysis
     */
    public int getReplicateCount() {
        return _replicateCount;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof DropoutEstimation) {
            final DropoutEstimation other = (DropoutEstimation) obj;
            return getMinimum().equals(other.getMinimum()) && getMaximum().equals(other.getMaximum());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(_minimum);
        hash = 97 * hash + Objects.hashCode(_maximum);
        return hash;
    }

    @Override
    public String toString() {
        return "5%: " + _minimum + " 95%: " + _maximum;
    }

    /**
     * Adds the results for a hypothesis to this object.
     *
     * @param hypothesisName the name of the hypothesis for which to add results
     * @param minimum the minimum dropout for the hypothesis
     * @param maximum the maximum dropout for the hypothesis
     */
    public void setValues(final String hypothesisName, final BigDecimal minimum, final BigDecimal maximum) {
        if (_minimum.compareTo(minimum) > 0) {
            _minimum = minimum.setScale(2, RoundingMode.HALF_UP);
        }

        if (_maximum.compareTo(maximum) < 0) {
            _maximum = maximum.setScale(2, RoundingMode.HALF_UP);
        }
        _minimumValues.put(hypothesisName, minimum.setScale(2, RoundingMode.HALF_UP));
        _maximumValues.put(hypothesisName, maximum.setScale(2, RoundingMode.HALF_UP));

        _isValid = _minimumValues.size() == 2 && _maximumValues.size() == 2;
    }

    /**
     * Sets the number of iterations over which the dropout estimation was performed.
     *
     * @param iterations the number of iterations
     */
    public void setIterations(final int iterations) {
        _iterations = iterations;
    }

    /**
     * Gets the number of iterations over which the dropout estimation was performed.
     *
     * @return the number of dropout estimation iterations
     */
    public int getIterations() {
        return _iterations;
    }

    public List<DropoutEstimationValue> getData() {
        return _data;
    }

    public void setData(final ArrayList<BigDecimal> succesfulDropouts) {
        _data = new ArrayList<>();
        for (int idx = 0; idx < 100; idx++) {
            _data.add(new DropoutEstimationValue(new BigDecimal(idx).divide(new BigDecimal(100)).round(new MathContext(2, RoundingMode.HALF_UP)).doubleValue()));
        }

        for (final BigDecimal dropout : succesfulDropouts) {
            _data.get(dropout.multiply(new BigDecimal(100)).intValue()).add();
        }
    }
}
