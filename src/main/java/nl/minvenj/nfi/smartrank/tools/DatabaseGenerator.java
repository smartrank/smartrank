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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.io.statistics.defaultcsv.DefaultStatisticsReader;

public class DatabaseGenerator {

    private static final String[] COUNTRYCODES = {"NL", "UK", "DE", "BE", "FR", "LU", "NO", "ES"};
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String[] LOCI = {"D7S820", "FGA", "D1S1656", "TPOX", "D19S433", "D5S818", "D10S1248", "PENTA_D", "D22S1045", "D2S1338", "PENTA_E", "D21S11", "D12S391", "D18S51", "TH01", "SE33", "D16S539", "D13S317", "CSF1PO", "D2S441", "D3S1358", "D8S1179", "vWA"};
    private static final double[] PROBABILITIES = {
        0.592405443,
        0.992402925,
        0.31377729,
        0.56017886,
        0.896153637,
        0.59539697,
        0.313674758,
        0.244255015,
        0.311269232,
        0.890389033,
        0.244018165,
        0.994943206,
        0.312411534,
        0.990185284,
        0.991634203,
        0.161416628,
        0.958158171,
        0.593697926,
        0.548390764,
        0.313684318,
        0.995981295,
        0.995698258,
        0.999681256};

    private static String generateRandomProfileName(final int idx) {
        final StringBuilder id = new StringBuilder();
        for (int curchar = 0; curchar < 4; curchar++) {
            id.append(CHARACTERS.charAt((int) (Math.random() * 26)));
        }

        String number = "00000000" + idx;
        number = number.substring(number.length() - 6);

        id.append(number.substring(0, 4)).append(COUNTRYCODES[(int) (Math.random() * COUNTRYCODES.length)]).append("#").append(number.substring(4));

        return id.toString();
    }

    private static class TextWriter {

        private final OutputStream _os;

        public TextWriter(final OutputStream os) {
            _os = os;
        }

        public TextWriter(final File f) throws FileNotFoundException {
            this(new FileOutputStream(f));
        }

        public void print(final String text) throws IOException {
            _os.write(text.getBytes());
        }

        public void println(final String text) throws IOException {
            print(text + "\n");
        }

        private void close() throws IOException {
            _os.close();
        }
    }

    public static void main(final String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException {

        final int recordCount = Integer.parseInt(args[0]);
        final PopulationStatistics stats = new DefaultStatisticsReader(args[1]).getStatistics();
        final String outputFolder = args.length>2?args[2]:"";
        final File db = new File(outputFolder, "database_codis_generated_" + recordCount + ".csv");
        db.delete();

        final TextWriter w = new TextWriter(db);
        w.print("specimenId");

        for (final String locusName : LOCI) {
            for (int idx = 1; idx < 5; idx++) {
                w.print("," + locusName + "_" + idx);
            }
        }
        w.println("");

        final int factor = Math.min(Math.max(recordCount, 10) / 10, 5000);
        for (int idx = 0; idx < recordCount; idx++) {
            if (idx % factor == 0) {
                System.out.println("Record #" + idx);
            }
            w.print(generateRandomProfileName(idx));
            for (int locusIdx = 0; locusIdx < LOCI.length; locusIdx++) {
                final String locusName = LOCI[locusIdx];
                final double probability = PROBABILITIES[locusIdx];

                final ArrayList<String> alleles = new ArrayList<>();
                if (SecureRandom.getInstance("SHA1PRNG").nextDouble() < probability) {
                    addRandomAllele(stats, locusName, alleles);
                    addRandomAllele(stats, locusName, alleles);
                }

                Collections.sort(alleles);
                for (int alleleIdx = 0; alleleIdx < 4; alleleIdx++) {
                    w.print(",");
                    if (alleleIdx < alleles.size()) {
                        w.print(alleles.get(alleleIdx));
                    }
                    else {
                        w.print("NULL");
                    }
                }
            }

            // A 0.001% chance of a corrupt record
            if (SecureRandom.getInstance("SHA1PRNG").nextInt(100000) < 1) {
                w.print(",");
                w.print("record-intentionally-corrupt");
            }
            w.println("");
        }
        w.close();
        System.out.println("Generated database: " + db.getAbsolutePath());
    }

    private static void addRandomAllele(final PopulationStatistics stats, final String locusName, final ArrayList<String> alleles) throws NoSuchAlgorithmException {
        final String randomAllele = generateRandomAllele(locusName, stats);
        // 5% of all homozygotic loci are represented by a single allele
        if (SecureRandom.getInstance("SHA1PRNG").nextInt(100)<5 || !alleles.contains(randomAllele)) {
            alleles.add(randomAllele);
        }
    }

    private static String generateRandomAllele(final String locusName, final PopulationStatistics stats) throws NoSuchAlgorithmException {
        final String[] fractions = {"", ".0", ".1", ".2", ".3"};
        final SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");

        double d = rnd.nextDouble();
        for (final String allele : stats.getAlleles(locusName)) {
            d -= stats.getProbability(locusName, allele);
            if (d <= 0) {
                return allele;
            }
        }

        // generate a rare allele
        final Collection<String> alleles = stats.getAlleles(locusName);
        String rareAllele = "" + (10 + rnd.nextInt(30)) + fractions[rnd.nextInt(fractions.length)];
        while (!alleles.contains(rareAllele)) {
            rareAllele = "" + (10 + rnd.nextInt(30)) + fractions[rnd.nextInt(fractions.length)];
        }
        return rareAllele;
    }

}