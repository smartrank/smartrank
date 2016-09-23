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

public interface SampleFileReaderFactory {

    /**
     * Determines whether the FileReader created by this factory can parse the
     * data in the supplied URI.
     *
     * @param file The file from which the data is to be read.
     *
     * @return true if the data can be parsed by the FileReader instance
     * returned by the create method
     */
    public boolean accepts(File file);

    /**
     * Creates a new FileReader instance to read the data at the specified URI
     *
     * @param file The file from which the data is to be read
     *
     * @return A FileReader instance to read the data at the supplied URI.
     */
    public SampleFileReader create(File file);

}
