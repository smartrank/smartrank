/*
 * Copyright (C) 2019 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.io.databases.jdbc.drivers;

import java.util.Properties;

import nl.minvenj.nfi.smartrank.SmartRank;
import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.io.databases.jdbc.JDBCDriverWrapper;

public class SQLServerDriverWrapper implements JDBCDriverWrapper {

    @Override
    public String getConnectString(final DatabaseConfiguration databaseConfiguration) {
        return "jdbc:jtds:sqlserver://" + databaseConfiguration.getHostAndPort() + "/" + databaseConfiguration.getSchemaName();
    }

    @Override
    public String toString() {
        return "SQLServer";
    }

    @Override
    public String getResultSizeQuery(final String query) {
        return "SELECT count(*) from (" + query + ") as countAlias";
    }

    @Override
    public String getConnectionTestQuery() {
        return "select TABLE_NAME from INFORMATION_SCHEMA.TABLES";
    }

    @Override
    public Properties getProperties(final DatabaseConfiguration config) {
        final Properties props = new Properties();
        props.put("user", config.getUserName());
        props.put("password", config.getPassword());
        props.put("appName", "SmartRank");
        props.put("progName", "SmartRank" + SmartRank.getVersion());
        return props;
    }
}
