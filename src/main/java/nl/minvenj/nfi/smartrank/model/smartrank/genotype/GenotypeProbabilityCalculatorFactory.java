/**
 * Copyright (C) 2013-2105 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.model.smartrank.genotype;

import nl.minvenj.nfi.smartrank.domain.Hypothesis;

/**
 *
 * @author dejong
 */
public class GenotypeProbabilityCalculatorFactory {

    /**
     * Private constructor to stop people from creating instances of this class.
     */
    private GenotypeProbabilityCalculatorFactory() {
    }

    /**
     * Factory method for GenotypeProbabilityCalculator implementations.
     *
     * @param hypothesis The current hypothesis
     * @return An implementation of the GenotypeProbabilityCalculator interface
     * for unrelated unknowns
     */
    public static GenotypeProbabilityCalculator getUnrelatedGenotypeProbabilityCalculator(Hypothesis hypothesis) {
        if (hypothesis.getThetaCorrection() == 0) {
            return new HardyWeinbergGenotypeProbabilityCalculator(hypothesis);
        }
        return new SplitDropGenotypeProbabilityCalculator(hypothesis);
    }
}
