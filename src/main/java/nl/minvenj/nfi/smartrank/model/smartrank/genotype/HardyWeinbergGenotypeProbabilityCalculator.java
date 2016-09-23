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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;

/**
 * Calculates a genotype's probability according to Hardy/Weinberg.
 *
 * @author dejong
 */
public class HardyWeinbergGenotypeProbabilityCalculator implements GenotypeProbabilityCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(HardyWeinbergGenotypeProbabilityCalculator.class);
    private final PopulationStatistics _populationStatistics;

    HardyWeinbergGenotypeProbabilityCalculator(Hypothesis hypothesis) {
        _populationStatistics = hypothesis.getPopulationStatistics();
    }

    @Override
    public double calculate(int[] alleleCounts, Locus locus) {
        double genotypeProbability = 1;
        if (!locus.isHomozygote()) {
            genotypeProbability = 2;
        }
        for (Allele allele : locus.getAlleles()) {
            genotypeProbability *= _populationStatistics.getProbability(locus, allele);
        }

        return genotypeProbability;
    }

    public String toLogString(Locus locus) {
        String retval = locus.getName() + ":" + locus.getAlleles();
        return retval;
    }
}
