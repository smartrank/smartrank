package nl.minvenj.nfi.smartrank.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

public class DatabaseRecordImporter {

    public static void main(final String[] args) throws IOException {
        String[] dbLoci = null;
        try (final BufferedReader reader = new BufferedReader(new FileReader(args[0]))) {
            dbLoci = readLocusNames(reader);
        }

        try (FileOutputStream fos = new FileOutputStream(new File(args[0]), true)) {
            try (final BufferedReader r = new BufferedReader(new FileReader(args[1]))) {

                LinkedHashMap<String, String> sample;
                final String[] sampleLoci = readLocusNames(r);

                for (final String sampleLocus : sampleLoci) {
                    boolean found = false;
                    for (int idx = 0; !found && idx < dbLoci.length; idx++) {
                        found |= dbLoci[idx].equalsIgnoreCase(sampleLocus);
                    }
                    if (!found) {
                        System.out.println("Sample Locus " + sampleLocus + " not found in target db!");
                    }
                }

                while ((sample = readSample(r, sampleLoci)) != null) {
                    writeSample(sample, dbLoci, fos);
                }
            }
        }
    }

    private static void writeSample(final LinkedHashMap<String, String> sample, final String[] dbLoci, final FileOutputStream fos) throws IOException {
        final StringBuilder sb = new StringBuilder();
        for (final String dbField : dbLoci) {
            String value = sample.get(dbField);
            if (value == null)
                value = "NULL";
            sb.append(",").append(value);
        }

        sb.append("\n");
        fos.write(sb.toString().substring(1).getBytes());
    }

    private static String[] readLocusNames(final BufferedReader r) throws IOException {
        final String headerLine = r.readLine();
        final String separator = determineSeparator(headerLine);
        final String[] fieldNames = headerLine.split(separator);
        for (int idx = 0; idx < fieldNames.length; idx++) {
            fieldNames[idx] = fieldNames[idx].trim().toUpperCase().replaceAll("[ _]", "");
        }
        return fieldNames;
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

    private static LinkedHashMap<String, String> readSample(final BufferedReader r, final String[] fieldNames) throws IOException {
        final String line = r.readLine();
        if (line == null)
            return null;
        final String separator = determineSeparator(line);
        final LinkedHashMap<String, String> retval = new LinkedHashMap<>();
        final String[] fieldValues = line.split(separator);
        for (int idx = 0; idx < fieldNames.length; idx++) {
            retval.put(fieldNames[idx].trim(), fieldValues[idx].trim());
        }
        return retval;
    }
}
