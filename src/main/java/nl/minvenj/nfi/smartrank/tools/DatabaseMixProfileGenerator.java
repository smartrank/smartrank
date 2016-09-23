package nl.minvenj.nfi.smartrank.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class DatabaseMixProfileGenerator {
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
    }

    public static void main(final String[] args) throws IOException {

        final File dbFile = new File(args[0]);

        final int donorCount = Integer.parseInt(args[1]);
        final double[] dropouts = new double[donorCount];
        for (int idx = 0; idx < donorCount; idx++) {
            if (idx + 2 < args.length)
                dropouts[idx] = Double.parseDouble(args[idx + 2]);
            else
                dropouts[idx] = dropouts[idx - 1];
        }

        final SecureRandom sr = new SecureRandom();
        sr.setSeed(System.currentTimeMillis());

        final String[] loci;
        int numberOfProfiles = 0;
        try (final BufferedReader r = new BufferedReader(new FileReader(dbFile))) {
            String headerLine = r.readLine();
            final String separator = determineSeparator(headerLine);
            headerLine = headerLine.substring(headerLine.indexOf(separator) + 1).replaceAll(" ", "").replaceAll("_\\d", "");
            loci = headerLine.split(separator);

            while (r.readLine() != null) {
                numberOfProfiles++;
            }
        }

        // Estimate number of profiles
        final double factor = (double) donorCount / (double) numberOfProfiles;

        // Select profiles to mix
        final ArrayList<String> donors = new ArrayList<>();

        while (donors.size() < donorCount) {
            try (final BufferedReader r = new BufferedReader(new FileReader(dbFile))) {
                String line;
                r.readLine(); // Skip header
                while (donors.size() < donorCount && (line = r.readLine()) != null) {
                    if (sr.nextDouble() < factor)
                        if (!donors.contains(line))
                            donors.add(line);
                }
            }
        }

        // Compose mix profile
        String mixProfileName = "";
        final HashMap<String, ArrayList<String>> mixProfile = new HashMap<>();
        int donorIdx = 0;
        for (final String donor : donors) {
            final String separator = determineSeparator(donor);

            final String donorName = donor.substring(0, donor.indexOf(separator)).trim();
            if (!mixProfile.isEmpty())
                mixProfileName += "-";
            mixProfileName += donorName;

            final String[] donorAlleles = donor.substring(donor.indexOf(separator) + 1).split(separator);
            for (int idx = 0; idx < donorAlleles.length; idx++) {

                final String locusName = loci[idx];
                final String allele = donorAlleles[idx];

                ArrayList<String> mixLocusAlleles = mixProfile.get(locusName);
                if (mixLocusAlleles == null) {
                    mixLocusAlleles = new ArrayList<String>();
                }

                if (!allele.equalsIgnoreCase("NULL") && sr.nextDouble() > dropouts[donorIdx] && !mixLocusAlleles.contains(allele)) {
                    mixLocusAlleles.add(allele);
                }

                if (!mixLocusAlleles.isEmpty()) {
                    mixProfile.put(locusName, mixLocusAlleles);
                }
            }
            donorIdx++;
        }

        // Save mix profile
        final File profile = new File(dbFile.getParent(), mixProfileName + ".csv");
        profile.delete();
        final TextWriter w = new TextWriter(profile);
        w.println("SampleName,Marker,Allele1,Allele2,Allele3,Allele4,Allele5,Allele6,Allele7,Allele8");
        for (final String locus : mixProfile.keySet()) {
            w.print(mixProfileName + "," + locus);
            int alleleCount = 0;
            final ArrayList<String> alleleList = mixProfile.get(locus);
            Collections.sort(alleleList, new Comparator<String>() {
                @Override
                public int compare(final String o1, final String o2) {
                    Double d1 = null;
                    Double d2 = null;
                    try {
                        d1 = new Double(o1);
                    }
                    catch (final NumberFormatException nfe) {
                    }
                    try {
                        d2 = new Double(o2);
                    }
                    catch (final NumberFormatException nfe) {
                    }
                    if (d1 == null && d2 == null)
                        return 0;
                    if (d1 == null)
                        return -1;
                    if (d2 == null)
                        return 1;
                    return new Double(o1).compareTo(new Double(o2));
                }
            });
            for (final String allele : alleleList) {
                alleleCount++;
                w.print("," + allele);
            }
            w.println(",,,,,,,,".substring(alleleCount));
        }

        System.out.println("Mix profile generated: " + profile);
    }

    private static String determineSeparator(final String line) {
        final String[] separators = {",", "\t", ";"};
        int separatorIndex = line.length();
        String sep = null;
        for (final String separator : separators) {
            final int idx = line.indexOf(separator);
            if (idx >= 0 && idx < separatorIndex) {
                separatorIndex = idx;
                sep = separator;
            }
        }
        return sep;
    }

}
