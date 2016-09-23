package nl.minvenj.nfi.smartrank.io.databases.jdbc;

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
     * @return a String containing the qeuery that will fetch the number of rows that will be returned when the supplied query is run, or null if this database mapping does not support this.
     */
    String getResultSizeQuery(String query);

}
