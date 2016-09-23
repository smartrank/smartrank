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

import nl.minvenj.nfi.smartrank.domain.Locus;

/**
 * An interface for classes that calculate genotype probabilities.
 *
 * @author dejong
 */
public interface GenotypeProbabilityCalculator {

    /**
     * Calculates the genotype probability for the supplied locus.
     *
     * @param alleleCounts An array of integers containing the allele counts of
     * all alleles up to the specified locus
     * @param locus The locus for which the genotype probability is to be
     * calculated
     * @return a double containing the probability of observing the supplied
     * genotype
     */
    public double calculate(int[] alleleCounts, Locus locus);
}
