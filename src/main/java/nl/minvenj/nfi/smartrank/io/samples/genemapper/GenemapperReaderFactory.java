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

package nl.minvenj.nfi.smartrank.io.samples.genemapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import nl.minvenj.nfi.smartrank.io.samples.SampleFileReader;
import nl.minvenj.nfi.smartrank.io.samples.SampleFileReaderFactory;

public class GenemapperReaderFactory implements SampleFileReaderFactory {

    private static final byte[] GENEMAPPER_SIGNATURE_BYTES = {
        (byte) 0x53, (byte) 0x61, (byte) 0x6D, (byte) 0x70, (byte) 0x6C, (byte) 0x65,
        (byte) 0x20, (byte) 0x46, (byte) 0x69, (byte) 0x6C, (byte) 0x65
    };

    @Override
    public boolean accepts(File file) {
        try {
            try (FileInputStream is = new FileInputStream(file)) {
                byte[] line = new byte[GENEMAPPER_SIGNATURE_BYTES.length];
                is.read(line);
                return Arrays.equals(line, GENEMAPPER_SIGNATURE_BYTES);
            }
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public SampleFileReader create(File file) {
        return new GenemapperFileReader(file);
    }

}
