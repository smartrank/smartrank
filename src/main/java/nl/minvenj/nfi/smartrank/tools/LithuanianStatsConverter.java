/**
    MIT License

    Copyright (c) 2016 Netherlands Forensic Institute

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package nl.minvenj.nfi.smartrank.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class LithuanianStatsConverter {

    public static void main(final String[] args) throws IOException {

        final ArrayList<String> encounteredAlleles = new ArrayList<>();
        final ArrayList<String> encounteredLoci = new ArrayList<>();
        final HashMap<String, String> stats = new HashMap<>();

        // Read from original file
        File inFile;
        if (args.length > 0)
            inFile = new File(args[0]);
        else {
            final JFileChooser chooser = new JFileChooser();
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(null))
                inFile = chooser.getSelectedFile();
            else
                return;
        }

        final File outFile = new File(inFile.getAbsolutePath() + ".csv");
        if (outFile.exists()) {
            JOptionPane.showMessageDialog(null, "File '" + outFile + "' already exists!", "Stats conversion error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inFile))) {
            String line;
            String currentLocus = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Rest Allele")) {
                }
                if (line.matches("[A-Z0-9]+\\s*")) {
                    currentLocus = line.trim();
                    encounteredLoci.add(currentLocus);
                }
                if (line.matches("\\d+(\\.\\d)?\\s0\\.\\d+")) {
                    final String[] strings = line.split("\t");
                    final String allele = strings[0];
                    final String frequency = strings[1];
                    if (!encounteredAlleles.contains(allele)) {
                        encounteredAlleles.add(allele);
                    }
                    stats.put(currentLocus + "-" + allele, frequency);
                }
            }
        }
        catch (final Throwable t) {
            JOptionPane.showMessageDialog(null, "Failed to read from '" + inFile + "':\n" + t.getClass().getSimpleName() + ": " + t.getLocalizedMessage(), "Stats conversion error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Sort alleles in ascending order (not strictly speaking required, but looks nice in the file)
        Collections.sort(encounteredAlleles, new Comparator<String>() {
            @Override
            public int compare(final String o1, final String o2) {
                return normalize(o1).compareTo(normalize(o2));
            }

            private String normalize(final String s) {
                if (s.matches("\\d+")) {
                    return normalize(s + ".0");
                }
                if (s.matches("\\d(\\.\\d+)?")) {
                    return normalize("0" + s);
                }
                return s;
            }

        });

        // Write to output file
        try (FileWriter writer = new FileWriter(outFile, false)) {
            final StringBuilder header = new StringBuilder("\"Allele\"");
            for (final String locusName : encounteredLoci) {
                header.append(",\"").append(locusName).append("\"");
            }
            header.append("\n");
            writer.write(header.toString());
            for (final String allele : encounteredAlleles) {
                writer.write("\"" + allele + "\"");
                for (final String locusName : encounteredLoci) {
                    final String freq = stats.get(locusName + "-" + allele);
                    writer.write(",");
                    if (freq != null) {
                        writer.write("\"" + freq + "\"");
                    }
                }
                writer.write("\n");
            }
        }
        catch (final Throwable t) {
            JOptionPane.showMessageDialog(null, "Failed to write to '" + outFile + "':\n" + t.getClass().getSimpleName() + ": " + t.getLocalizedMessage(), "Stats conversion error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

}
