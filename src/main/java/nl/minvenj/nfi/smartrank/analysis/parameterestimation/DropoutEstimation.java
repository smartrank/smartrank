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
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;

/**
 * Records the results of a dropout estimation.
 */
public class DropoutEstimation {

    private int _replicateCount;
    private int _iterations;
    private List<DropoutEstimationValue> _data;
    private double[] _doubleData;
    private final int _percentile;

    public DropoutEstimation() {
        _percentile = SmartRankRestrictions.getDropoutEstimationPercentile();
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
    public String toString() {
        return "5%: " + getPercentile(5) + " 50%: " + getPercentile(50) + " 95%: " + getPercentile(95);
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

    /**
     * Gets the count for each dropout value of the number of iterations of the monte carlo simulation where the observed number of alleles was recovered.
     *
     * @return A {@link List} of {@link DropoutEstimationValue}s
     */
    public List<DropoutEstimationValue> getData() {
        return _data;
    }

    /**
     * Sets the dropout values for which the number of alleles observed in the evidence was recovered during a Monte Carlo simulation.
     *
     * @param succesfulDropouts A {@link Collection} of  {@link BigDecimal}s representing the dropout values for which the observed number of alleles was recovered
     */
    public void setData(final Collection<BigDecimal> succesfulDropouts) {
        _data = new ArrayList<>();
        _doubleData = new double[succesfulDropouts.size()];
        for (int idx = 0; idx < 100; idx++) {
            _data.add(new DropoutEstimationValue(new BigDecimal(idx).divide(new BigDecimal(100)).round(new MathContext(2, RoundingMode.HALF_UP)).doubleValue()));
        }

        int idx = 0;
        for (final BigDecimal dropout : succesfulDropouts) {
            _data.get(dropout.multiply(new BigDecimal(100)).intValue()).add();
            _doubleData[idx++] = dropout.round(new MathContext(2, RoundingMode.HALF_UP)).doubleValue();
        }
    }

    /**
     * Gets the percentile configured in the restrictions file of the dropout values distribution to use as final dropout result of the estimation.
     *
     * @return a {@link BigDecimal} containing the estimation of the dropout
     */
    public BigDecimal getEstimatedDropout() {
        return getPercentile(_percentile);
    }

    /**
     * Gets a percentile point of the distribution of successful dropouts.
     *
     * @param percentile The requested percentile.
     *
     * @return a {@link BigDecimal} containing the dropout at the requested percentile
     */
    public BigDecimal getPercentile(final int percentile) {
        final Percentile p = new Percentile(percentile);
        p.setData(_doubleData);
        return new BigDecimal(p.evaluate()).round(new MathContext(2, RoundingMode.HALF_UP));
    }

    /**
     * Gets the percentile used to estimate the dropout value.
     *
     * @return an integer containing the percentile of the dropout distribution to use when estimating dropout
     */
    public int getDropoutEstimationPercentile() {
        return _percentile;
    }
}
