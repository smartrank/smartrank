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

package nl.minvenj.nfi.smartrank.io.samples.codis;

import java.io.File;

import nl.minvenj.nfi.smartrank.io.samples.SampleFileReader;
import nl.minvenj.nfi.smartrank.io.samples.SampleFileReaderFactory;

public class CodisImportFileReaderFactory implements SampleFileReaderFactory {

    @Override
    public boolean accepts(final File file) {
        return file.getName().toLowerCase().endsWith(".xml");
    }

    @Override
    public SampleFileReader create(final File file) {
        return new CodisFileReader(file);
    }

}
