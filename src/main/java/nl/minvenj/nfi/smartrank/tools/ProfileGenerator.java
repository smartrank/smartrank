/*
 * Copyright (C) 2015 Netherlands Forensic Institute
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.minvenj.nfi.smartrank.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.samples.SampleWriter;
import nl.minvenj.nfi.smartrank.io.statistics.defaultcsv.DefaultStatisticsReader;

public class ProfileGenerator {

    private static final String[] COUNTRYCODES = {"NL", "UK", "DE", "BE", "FR", "LU", "NO", "ES"};
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static SecureRandom _rnd;

    public static void main(final String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException {

        final PopulationStatistics stats = new DefaultStatisticsReader(args[0]).getStatistics();

        _rnd = SecureRandom.getInstance("SHA1PRNG");
        _rnd.setSeed(System.currentTimeMillis());

        final String randomProfileName = generateRandomProfileName();
        final Sample randomProfile = generateRandomProfile(stats, randomProfileName);
        final File outputFile = new File(args[1], randomProfileName + ".csv");
        final SampleWriter writer = new SampleWriter(outputFile);
        writer.write(randomProfile);

        System.out.println("Generated random profile: " + outputFile);
    }

    private static Sample generateRandomProfile(final PopulationStatistics stats, final String randomProfileName) {
        final Sample profile = new Sample(randomProfileName);
        for (final String locusName : stats.getLoci()) {
            final Locus locus = new Locus(locusName);
            profile.addLocus(locus);
            locus.addAllele(generateRandomAllele(locusName, stats));
            locus.addAllele(generateRandomAllele(locusName, stats));
        }
        return profile;
    }

    private static String generateRandomProfileName() {
        final StringBuilder id = new StringBuilder();
        for (int curchar = 0; curchar < 4; curchar++) {
            id.append(CHARACTERS.charAt((int) (Math.random() * 26)));
        }

        String number = "00000000" + (int) (Math.random() * 10);
        number = number.substring(number.length() - 6);
        id.append(number.substring(0, 4)).append(COUNTRYCODES[(int) (Math.random() * COUNTRYCODES.length)]).append("#").append(number.substring(4));
        return id.toString();
    }

    private static Allele generateRandomAllele(final String locusName, final PopulationStatistics stats) {
        while (true) {
            double d = _rnd.nextDouble();
            for (final String allele : stats.getAlleles(locusName)) {
                d -= stats.getProbability(locusName, allele);
                if (d <= 0) {
                    return new Allele(allele);
                }
            }
        }
    }

}