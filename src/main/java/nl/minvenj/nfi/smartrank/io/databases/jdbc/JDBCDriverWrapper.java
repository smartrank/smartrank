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
package nl.minvenj.nfi.smartrank.io.databases.jdbc;

import java.util.Properties;

import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;

public interface JDBCDriverWrapper {

    /**
     * Gets a connection url string in the correct format for the wrapped JDBC driver.
     *
     * @param databaseConfiguration the {@link DatabaseConfiguration} containing the connection details
     * @return a string containing the connection url for the supplied configuration
     */
    String getConnectString(DatabaseConfiguration databaseConfiguration);

    /**
     * Returns a query that fetches the number of rows that will be returned by the supplied query.
     *
     * @param query the query to get the result size for
     * @return a String containing the query that will fetch the number of rows that will be returned when the supplied query is run, or null if this database mapping does not support this.
     */
    String getResultSizeQuery(String query);

    /**
     * Returns a string containing a query to be used to test the validity of a database connection.
     *
     * @return a String containing the query that tests the validity of the current database connection
     */
    String getConnectionTestQuery();

    /**
     * Creates a new Properties object from the supplied configuration.
     *
     * @param databaseConfiguration the Dataabseconfiguration that contains the values with which to connect to the database
     *
     * @return a Properties object containing the relevant settings to connect to the database for this database type
     */
    Properties getProperties(DatabaseConfiguration databaseConfiguration);

}
