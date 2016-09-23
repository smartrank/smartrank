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
package nl.minvenj.nfi.smartrank.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * This class represents a DNA _sample when evaluated as a (non-)contributor in
 * the context of a hypothesis.
 */
public class Contributor implements Cloneable {

    private Sample _sample;
    private boolean _candidate;
    private double _dropOutProbability = 0;
    private double _dropOutProbabilityHomozygote = 0;

    /**
     * Constructs a new Contributor class based on a {@link Sample} and dropout
     * probability
     *
     * @param sample             The _sample to be evaluated
     * @param dropOutProbability The dropout probability for this _sample
     */
    public Contributor(final Sample sample, final double dropOutProbability) {
        this(sample, dropOutProbability, false);
    }

    /**
     * Constructs a new Contributor class based on a {@link Sample} and dropout
     * probability
     *
     * @param sample             The _sample to be evaluated
     * @param dropOutProbability The dropout probability for this _sample
     */
    public Contributor(final Sample sample, final double dropOutProbability, final boolean candidate) {
        setProbabilities(dropOutProbability);
        _sample = sample;
        _candidate = candidate;
    }

    /**
     * Constructs a new Contributor class by copying an existing Contributor
     * instance. The new Contributor will contain a reference to the _sample of
     * the original but have an independent dropout value.
     *
     * @param original The original contributor to copy
     */
    public Contributor(final Contributor original) {
        setProbabilities(original._dropOutProbability);
        _sample = original._sample;
        _candidate = original._candidate;
    }

    /**
     * Gets the dropout probability for this contributor
     *
     * @param homozygote true if the returned value is to be used for a
     * homozygote locus
     * @return the dropout probability for this contributor
     */
    public double getDropOutProbability(final boolean homozygote) {
        return homozygote ? _dropOutProbabilityHomozygote : _dropOutProbability;
    }

    /**
     * @return the contributor's sample
     */
    public Sample getSample() {
        return _sample;
    }

    /**
     * Sets the dropout probability for this contributor
     *
     * @param dropout The new value for the dropout probability
     */
    public void setDropoutProbability(final double dropout) {
        setProbabilities(dropout);
    }

    /**
     * Gets the dropout probability for this contributor
     */
    public double getDropoutProbability() {
        return _dropOutProbability;
    }

    /**
     * sets the dropout probabilities for the various scenarios (present once,
     * present multiple times, dropped out)
     *
     * @param dropOutProbability The global dropout probability for this
     * contributor
     */
    private void setProbabilities(final double dropOutProbability) {
        _dropOutProbability = dropOutProbability;
        _dropOutProbabilityHomozygote = dropOutProbability * dropOutProbability;
    }

    @Override
    public String toString() {
        return _sample.getName() + "(" + new BigDecimal(_dropOutProbability, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP) + ")";
    }

    public void setSample(final Sample newSample) {
        _sample = newSample;
    }

    public boolean isCandidate() {
        return _candidate;
    }

    void setCandidate(final boolean candidate) {
        _candidate = candidate;
    }
}
