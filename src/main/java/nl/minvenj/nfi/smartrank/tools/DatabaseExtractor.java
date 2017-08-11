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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;

public class DatabaseExtractor {

    private static final String[] LOCI = {"D7S820", "FGA", "D1S1656", "TPOX", "D19S433", "D5S818", "D10S1248", "PENTA_D", "D22S1045", "D2S1338", "PENTA_E", "D21S11", "D12S391", "D18S51", "TH01", "SE33", "D16S539", "D13S317", "CSF1PO", "D2S441", "D3S1358", "D8S1179", "vWA", "F13A1",
        "FES",
        "DYS19",
        "DYS385",
        "DYS389I",
        "DYS389II",
        "DYS390",
        "DYS391",
        "DYS392",
        "DYS393",
        "DYS437",
        "DYS438",
        "DYS439",
        "DYS448",
        "DYS456",
        "DYS458",
        "DYS481",
        "DYS533",
        "DYS549",
        "DYS570",
        "DYS576",
        "DYS635",
        "DYS643",
        "YGATAH4",
        "D10S2325",
        "D21S2055",
        "D2S1360",
        "D3S1744",
        "D4S2366",
        "D5S2500",
        "D6S474",
        "D7S1517",
        "D8S1132"};

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

    private static ArrayList<String> _unknownLoci = new ArrayList<>();;

    public static void main(final String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException {

        final File inputLog = new File(args[0]);
        TextWriter w = null;

        try (BufferedReader br = new BufferedReader(new FileReader(inputLog))) {
            String line;
            Sample curSample = null;
            final Pattern startPattern = Pattern.compile("^.*Got 253016 specimen IDs.*$");
            final Pattern samplePattern = Pattern.compile("^.*Creating new sample (.*)$");
            final Pattern allelePattern = Pattern.compile("^.*Adding Allele ([^\\.]+)\\.(.+)$");
            int count = 0;
            while ((line = br.readLine()) != null) {
                final Matcher sampleMatcher = samplePattern.matcher(line);
                final Matcher startMatcher = startPattern.matcher(line);
                final Matcher alleleMatcher = allelePattern.matcher(line);
                if (startMatcher.matches()) {
                    w = openOutputFile(inputLog, count++);
                }
                if (sampleMatcher.matches()) {
                    writeSample(w, curSample);
                    curSample = new Sample(Locus.normalize(sampleMatcher.group(1)));
                }
                if (alleleMatcher.matches()) {
                    addAllele(curSample, alleleMatcher.group(1), alleleMatcher.group(2));
                }
            }
            writeSample(w, curSample);
        }
        w.close();
    }

    private static TextWriter openOutputFile(final File inputLog, final int i) throws IOException {
        final File outputCsv = new File(inputLog.getParentFile(), inputLog.getName() + "-" + i + ".csv");
        outputCsv.delete();

        System.out.println(outputCsv.getAbsolutePath());

        final TextWriter w = new TextWriter(outputCsv);
        w.print("specimenId");

        for (final String locusName : LOCI) {
            for (int idx = 1; idx < 5; idx++) {
                w.print("," + locusName + "_" + idx);
            }
        }
        w.println("");
        return w;
    }

    private static void writeSample(final TextWriter w, final Sample sample) throws IOException {
        if (sample == null)
            return;
        final StringBuilder sb = new StringBuilder(sample.getName());
        for (final String locusName : LOCI) {
            final Locus locus = sample.getLocus(locusName);
            final Allele[] alleles = locus == null ? new Allele[0] : locus.getAlleles().toArray(new Allele[0]);
            for (int idx = 0; idx < 4; idx++) {
                sb.append(",").append(idx >= alleles.length ? "NULL" : alleles[idx]);
            }
        }
        w.println(sb.toString());

        for (final Locus locus : sample.getLoci()) {
            if (!contains(locus.getName(), LOCI) && !contains(locus.getName(), _unknownLoci)) {
                System.out.println(locus.getName());
                _unknownLoci.add(locus.getName());
            }
        }
    }

    private static boolean contains(final String key, final String[] values) {
        for (final String value : values) {
            if (Locus.normalize(value).equals(key)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(final String key, final Collection<String> values) {
        for (final String value : values) {
            if (Locus.normalize(value).equals(key)) {
                return true;
            }
        }
        return false;
    }

    private static void addAllele(final Sample sample, final String locusName, final String allele) {
        Locus locus = sample.getLocus(locusName);
        if (locus == null) {
            locus = new Locus(locusName);
            sample.addLocus(locus);
        }
        locus.addAllele(new Allele(allele));
    }
}