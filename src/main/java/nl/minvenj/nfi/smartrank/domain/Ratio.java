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

import nl.minvenj.nfi.smartrank.raven.numberformat.NumberUtils;

public class Ratio {

    private final String _locusName;
    private final Double _defenseProbability;
    private final Double _prosecutionProbability;
    private final Double _ratio;

    /**
     * Constructor
     *
     * @param locusName the name of the locus for which this ratio applies
     * @param prosecutionProbability The probability for the prosecution
     * @param defenseProbability The probability for the defense
     */
    public Ratio(final String locusName, final Double prosecutionProbability, final Double defenseProbability) {
        this(locusName, prosecutionProbability, defenseProbability, null);
    }

    /**
     * Constructor
     *
     * @param locusName the name of the locus for which this ratio applies
     * @param prosecutionProbability The probability for the prosecution
     * @param defenseProbability The probability for the defense
     * @param ratio The ratio calculation over prosecution and defense
     * probabilities. Useful when the individual probabilities are too small to
     * calculate a ratio
     */
    public Ratio(final String locusName, final Double prosecutionProbability, final Double defenseProbability, final Double ratio) {
        _locusName = locusName;
        _defenseProbability = defenseProbability;
        _prosecutionProbability = prosecutionProbability;
        if ((prosecutionProbability == null || defenseProbability == null)) {
            _ratio = Double.NaN;
        } else {
            if (ratio != null) {
                _ratio = ratio;
            } else {
                _ratio = prosecutionProbability / defenseProbability;
            }
        }
    }

    /**
     * @return the _locusName
     */
    public String getLocusName() {
        return _locusName;
    }

    public Double getDefenseProbability() {
        return _defenseProbability;
    }

    public Double getProsecutionProbability() {
        return _prosecutionProbability;
    }

    /**
     * @return the _ratio
     */
    public Double getRatio() {
        return _ratio;
    }

    public boolean isReal() {
        return !(_ratio.isInfinite() || _ratio.isNaN());
    }

    @Override
    public String toString() {
        return NumberUtils.format(4, _ratio);
    }
}
