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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.domain.Sample;

/**
 * Generates a random profile based on the probabilities listed in the population statistics.
 */
public class RandomProfileGenerator {

    protected final SecureRandom _rnd;
    private final PopulationStatistics _stats;
    private final Collection<String> _enabledLoci;

    public RandomProfileGenerator(final Collection<String> enabledLoci, final PopulationStatistics stats) {
        this(enabledLoci, stats, new SecureRandom());
    }

    public RandomProfileGenerator(final Collection<String> enabledLoci, final PopulationStatistics stats, final SecureRandom rnd) {
        _stats = stats;
        _rnd = rnd;
        _rnd.setSeed(System.currentTimeMillis());
        _enabledLoci = enabledLoci;
    }

    public Sample getRandomSample() throws NoSuchAlgorithmException {
        final Sample sample = new Sample("RandomSample");

        for (final String locusName : _enabledLoci) {
            final Locus locus = new Locus(locusName);

            locus.addAllele(getRandomAllele(locusName));
            locus.addAllele(getRandomAllele(locusName));

            sample.addLocus(locus);
        }

        return sample;
    }

    /**
     * Generates a random allele for the getNamed locus
     *
     * @param locusName The getName of the target locus
     * @return a randomly generated allele
     */
    public Allele getRandomAllele(final String locusName) {

        while (true) {
            final double randomValue = _rnd.nextDouble();
            double threshold = 0.0;

            for (final String allele : _stats.getAlleles(locusName)) {
                threshold += _stats.getProbability(locusName, allele).doubleValue();
                if (randomValue <= threshold) {
                    return new Allele(allele);
                }
            }
        }
    }
}
