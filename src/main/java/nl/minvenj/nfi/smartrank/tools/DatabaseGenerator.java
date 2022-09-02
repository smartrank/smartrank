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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.io.statistics.defaultcsv.DefaultStatisticsReader;

public class DatabaseGenerator {

    private static final int MAX_ALLELES_PER_LOCUS = 2;
    private static final String[] COUNTRYCODES = {"NL", "UK", "DE", "BE", "FR", "LU", "NO", "ES"};
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String[] ALL_LOCI = {
        "AMEL", "CSF1PO", "D10S1248", "D12S391", "D13S317", "D16S539", "D18S51", "D19S433", "D1S1656", "D21S11", "D22S1045", "D2S1338", "D2S441", "YINDEL", "D3S1358", "D5S818", "F13A1", "DYS448", "DYS456", "DYS533", "D4S2366", "DYS481", "D7S820", "D8S1179", "DYS389I", "DYS389II", "DYS391", "DYS390", "DYS392", "DYS393", "DYS570", "DYS576", "DYS643", "FGA", "Penta D", "Penta E", "SE33", "TH01", "TPOX", "vWA"
    };
    private static final String[] PPF6C_LOCI = {
        "AMEL", "CSF1PO", "D10S1248", "D12S391", "D13S317", "D16S539", "D18S51", "D19S433", "D1S1656", "D21S11", "D22S1045", "D2S1338", "D2S441", "D3S1358", "D5S818", "D7S820", "D8S1179", "DYS391", "DYS570", "DYS576", "FGA", "Penta D", "Penta E", "SE33", "TH01", "TPOX", "vWA"
    };
    private static final String[] PPF6C_NOAMEL_LOCI = {
        "CSF1PO", "D10S1248", "D12S391", "D13S317", "D16S539", "D18S51", "D19S433", "D1S1656", "D21S11", "D22S1045", "D2S1338", "D2S441", "D3S1358", "D5S818", "D7S820", "D8S1179", "DYS391", "DYS570", "DYS576", "FGA", "Penta D", "Penta E", "SE33", "TH01", "TPOX", "vWA"
    };
    private static final String[] PPF6C_NOY_LOCI = {
        "AMEL", "CSF1PO", "D10S1248", "D12S391", "D13S317", "D16S539", "D18S51", "D19S433", "D1S1656", "D21S11", "D22S1045", "D2S1338", "D2S441", "D3S1358", "D5S818", "D7S820", "D8S1179", "FGA", "Penta D", "Penta E", "SE33", "TH01", "TPOX", "vWA"
    };
    private static final String[] NGM_LOCI = {
        "AMEL", "D10S1248", "D12S391", "D16S539", "D18S51", "D19S433", "D1S1656", "D21S11", "D22S1045", "D2S1338", "D2S441", "D3S1358", "D8S1179", "FGA", "TH01", "vWA"
    };
    private static final String[] NGM_NOAMEL_LOCI = {
        "D10S1248", "D12S391", "D16S539", "D18S51", "D19S433", "D1S1656", "D21S11", "D22S1045", "D2S1338", "D2S441", "D3S1358", "D8S1179", "FGA", "TH01", "vWA"
    };
    private static final String[] NGMSELECT_LOCI = {
        "AMEL", "D10S1248", "D12S391", "D16S539", "D18S51", "D19S433", "D1S1656", "D21S11", "D22S1045", "D2S1338", "D2S441", "D3S1358", "D8S1179", "FGA", "SE33", "TH01", "vWA"
    };
    private static final String[] SGMPLUS_LOCI = {
        "AMEL", "D16S539", "D18S51", "D19S433", "D21S11", "D2S1338", "D3S1358", "D8S1179", "FGA", "TH01", "vWA"
    };

    private static class Kit {
        public final String _name;
        public final List<String> _loci;
        public final Double _probability;

        public Kit(final String name, final Double probability, final List<String> loci) {
            this._name = name;
            this._probability = probability;
            this._loci = new ArrayList<>(loci);
        }
    }

    private static Kit[] _kits = {
        new Kit("PPF6C", 0.0861, Arrays.asList(PPF6C_LOCI)),
        new Kit("PPF6C_NOAMEL", 0.0098, Arrays.asList(PPF6C_NOAMEL_LOCI)),
        new Kit("PPF6C_NOY", 0.0211, Arrays.asList(PPF6C_NOY_LOCI)),
        new Kit("NGMSelect", 0.0109, Arrays.asList(NGMSELECT_LOCI)),
        new Kit("NGM", 0.4873, Arrays.asList(NGM_LOCI)),
        new Kit("NGM_NOAMEL", 0.0324, Arrays.asList(NGM_NOAMEL_LOCI)),
        new Kit("SGMPlus", 0.3007, Arrays.asList(SGMPLUS_LOCI)),
        new Kit("Other", Double.NaN, Arrays.asList(ALL_LOCI))
    };

    private static String generateRandomProfileName(final SecureRandom random, final String kitName, final int idx) {
        final StringBuilder id = new StringBuilder();
        for (int curchar = 0; curchar < 4; curchar++) {
            id.append(CHARACTERS.charAt((int) (random.nextDouble() * 26)));
        }

        String number = "00000000" + idx;
        number = number.substring(number.length() - 6);

        id.append(number.substring(0, 4)).append(COUNTRYCODES[(int) (random.nextDouble() * COUNTRYCODES.length)]).append("#").append(number.substring(4));

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

    public static void main(final String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException, InterruptedException {

        final int recordCount = Integer.parseInt(args[0]);
        final PopulationStatistics stats = new DefaultStatisticsReader(args[1]).getStatistics();
        final String outputFolder = args.length > 2 ? args[2] : "";
        final File db = new File(outputFolder, "database_generated_" + recordCount + ".csv");
        db.delete();

        final TextWriter w = new TextWriter(db);
        w.print("specimenId");

        for (final String locus : ALL_LOCI) {
            for (int idx = 1; idx <= MAX_ALLELES_PER_LOCUS; idx++) {
                w.print("," + locus + "_" + idx);
            }
        }
        w.println("");

        final HashMap<String, HashMap<String, Double>> myStats = new HashMap<>();
        for (final String locus : ALL_LOCI) {
            final HashMap<String, Double> alleleProbabilities = myStats.getOrDefault(locus, new HashMap<>());
            final Collection<String> alleles = stats.getAlleles(locus);
            for (final String allele : alleles) {
                alleleProbabilities.put(allele, stats.getProbability(locus, allele));
            }
            myStats.put(locus, alleleProbabilities);
        }

        LocalDateTime lastFeedbackTime = LocalDateTime.now().minusSeconds(30);
        for (int idx = 0; idx < recordCount; idx++) {
            if (LocalDateTime.now().minusSeconds(5).isAfter(lastFeedbackTime)) {
                lastFeedbackTime = LocalDateTime.now();
                System.out.println("Record #" + idx);
            }
            final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");

            final Kit randomKit = getRandomKit(secureRandom);

            final String profileName = generateRandomProfileName(secureRandom, randomKit._name, idx);
            w.print(profileName);

            // Create a random profile
            final HashMap<String, List<String>> lociInProfile = new HashMap<>();

            if (randomKit._probability.isNaN()) {
                while (lociInProfile.size() < 4) {
                    for (final String locus : randomKit._loci) {
                        // For a random kit, include a locus with 5% probability
                        if (secureRandom.nextDouble() < 0.05 && !lociInProfile.containsKey(locus)) {
                            final HashMap<String, Double> alleleProbabilities = myStats.get(locus);
                            final List<String> alleles = lociInProfile.getOrDefault(locus, new ArrayList<>());
                            addRandomAllele(secureRandom, alleleProbabilities, locus, alleles);
                            addRandomAllele(secureRandom, alleleProbabilities, locus, alleles);
                            Collections.sort(alleles);
                            lociInProfile.put(locus, alleles);
                        }
                    }
                }
            }
            else {
                for (final String locus : randomKit._loci) {
                    // For a random kit, include a locus with 5% probability
                    final HashMap<String, Double> alleleProbabilities = myStats.get(locus);
                    final List<String> alleles = lociInProfile.getOrDefault(locus, new ArrayList<>());
                    addRandomAllele(secureRandom, alleleProbabilities, locus, alleles);
                    addRandomAllele(secureRandom, alleleProbabilities, locus, alleles);
                    Collections.sort(alleles);
                    lociInProfile.put(locus, alleles);
                }
            }

            // Write generated profile to output
            for (final String locus : ALL_LOCI) {
                final List<String> alleles = lociInProfile.getOrDefault(locus, Collections.emptyList());
                for (int alleleIdx = 0; alleleIdx < MAX_ALLELES_PER_LOCUS; alleleIdx++) {
                    w.print(",");
                    if (alleleIdx < alleles.size()) {
                        w.print(alleles.get(alleleIdx));
                    }
                    else {
                        w.print("NULL");
                    }
                }
            }
            w.println("");
        }
        w.close();
        System.out.println("Generated database: " + db.getAbsolutePath());
    }

    private static Kit getRandomKit(final Random random) {
        double d = random.nextDouble();
        for (final Kit kit : _kits) {
            if (Double.isNaN(kit._probability)) {
                return kit;
            }
            d -= kit._probability;
            if (d <= 0) {
                return kit;
            }
        }
        return null;
    }

    private static void addRandomAllele(final Random random, final Map<String, Double> alleleProbabilities, final String locusName, final List<String> alleles) throws NoSuchAlgorithmException {
        final String randomAllele = generateRandomAllele(random, locusName, alleleProbabilities);
        // 5% of all homozygotic loci are represented by a single allele
        if (random.nextInt(100) < 5 || !alleles.contains(randomAllele)) {
            alleles.add(randomAllele);
        }
    }

    private static String generateRandomAllele(final Random random, final String locusName, final Map<String, Double> alleleProbabilities) throws NoSuchAlgorithmException {
        final String[] fractions = {"", ".0", ".1", ".2", ".3"};

        double d = random.nextDouble();
        final Collection<String> alleles = alleleProbabilities.keySet();
        for (final String allele : alleles) {
            d -= alleleProbabilities.get(allele);
            if (d <= 0) {
                return allele;
            }
        }

        // generate a rare allele
        String rareAllele = "" + (10 + random.nextInt(30)) + fractions[random.nextInt(fractions.length)];
        while (alleles.contains(rareAllele)) {
            rareAllele = "" + (10 + random.nextInt(30)) + fractions[random.nextInt(fractions.length)];
        }
        return rareAllele;
    }

}