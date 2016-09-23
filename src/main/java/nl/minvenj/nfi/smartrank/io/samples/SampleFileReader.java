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

package nl.minvenj.nfi.smartrank.io.samples;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import nl.minvenj.nfi.smartrank.domain.Sample;

public interface SampleFileReader {

    /**
     * Determines the file type and reads the samples within.
     *
     * @return A string containing the hex of the file data hash
     *
     * @throws IOException If there was an error reading from the file.
     */
    public String getFileHash() throws IOException;

    /**
     * @return A collection of {@link Sample} objects representing the samples
     * in the file
     * @throws java.io.IOException if an error occurs reading from the source
     *                             file
     */
    public Collection<Sample> getSamples() throws IOException;

    public String getCaseNumber() throws IOException;

    /**
     *
     * @return The File object representing the source file for this reader
     */
    public File getFile();

}
