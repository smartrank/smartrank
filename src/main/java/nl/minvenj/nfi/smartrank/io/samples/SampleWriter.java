/*
 * Copyright (C) 2016 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.io.samples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;

/**
 * Writes a sample to a CSV file.
 */
public class SampleWriter {

    private final File _outputFile;

    /**
     * Constructor.
     *
     * @param outputFile the file to write samples to
     */
    public SampleWriter(final File outputFile) {
        _outputFile = outputFile;
    }

    /**
     * Writes a sample to the file indicated in the constructor. If the target file is not empty, the new sample is appended to the end.
     *
     * @param profile the sample to write
     * @throws IOException if an error occurred writing the the file
     */
    public void write(final Sample profile) throws IOException {

        File outFile = _outputFile;
        int copyIndex = 0;
        while (outFile.exists()) {
            String path = outFile.getAbsolutePath();
            final int replaceFrom = path.lastIndexOf("-copy-");
            if (replaceFrom == -1) {
                path = path.replace(".csv", "-copy-0.csv");
            }
            else {
                path = path.substring(0, replaceFrom) + String.format("-copy-%d.csv", copyIndex++);
            }

            outFile = new File(path);
        }

        try (FileOutputStream fos = new FileOutputStream(outFile, true)) {
            // Write header if necessary
            if (_outputFile.length() == 0) {
                fos.write("SampleName,Marker,Allele1,Allele2,Allele3,Allele4\n".getBytes());
            }
            for (final Locus locus : profile.getLoci()) {
                fos.write((profile.getName() + "," + locus.getName() + ",").getBytes());
                int count = 0;
                for (final Allele allele : locus.getAlleles()) {
                    fos.write((allele.getAllele() + ",").getBytes());
                    count++;
                }
                fos.write(",,,\n".substring(count).getBytes());
            }
        }
    }

}
