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

package nl.minvenj.nfi.smartrank.io.databases;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.io.databases.codis.CodisDatabaseReader;
import nl.minvenj.nfi.smartrank.io.databases.jdbc.JDBCReader;

public class DatabaseReaderFactory {

    public static DatabaseReader create(final DatabaseConfiguration config) throws MalformedURLException, IOException {
        if (config.getConnectString().startsWith("jdbc:"))
            return new JDBCReader(config);
        return new CodisDatabaseReader(new File(config.getConnectString()));
    }

}
