package nl.minvenj.nfi.smartrank.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

public class DatabaseRecordExporter {
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

    public static void main(final String[] args) throws IOException {
        try (final BufferedReader r = new BufferedReader(new FileReader(args[0]))) {

            String line;
            String headerLine = r.readLine();
            // Determine separator
            char separator = ',';
            for (final char sep : ",\t;".toCharArray()) {
                if (headerLine.indexOf(sep) >= 0) {
                    separator = sep;
                    break;
                }
            }

            headerLine = headerLine.replaceAll(" ", "").replaceAll(separator + "[A-Za-z0-9_]+_(2|3|4)", "").replaceAll("_1", "").substring(11);
            final String[] loci = headerLine.split(",");

            Pattern pattern;
            if (args.length == 1) {
                pattern = Pattern.compile(".*");
            }
            else {
                try {
                    pattern = Pattern.compile(args[1]);
                }
                catch (final Throwable t) {
                    System.out.println("'" + args[1] + "' is not a valid Regex. Treating as fixed string.");
                    pattern = Pattern.compile(args[1], Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
                }
            }

            while ((line = r.readLine()) != null) {
                final String recordName = line.substring(0, line.indexOf(separator)).trim();
                if (pattern.matcher(recordName).matches()) {
                    final File f = new File(args[0]);
                    final File profile = new File(f.getParent(), toSafeFileName(recordName) + ".csv");
                    profile.delete();
                    final TextWriter w = new TextWriter(profile);
                    w.println("SampleName,Marker,Allele1,Allele2");
                    final String[] alleles = line.substring(line.indexOf(separator)).split(separator + "NULL" + separator + "NULL");
                    for (int idx = 0; idx < loci.length; idx++) {
                        w.println(recordName + "," + loci[idx] + alleles[idx]);
                    }
                    w.close();
                    break;
                }
            }
        }
    }

    private static String toSafeFileName(final String string) {
        return string.replaceAll("[^a-zA-Z0-9\\-]", "-");
    }

}
