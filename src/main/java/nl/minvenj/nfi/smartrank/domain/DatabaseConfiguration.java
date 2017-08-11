package nl.minvenj.nfi.smartrank.domain;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.io.databases.jdbc.JDBCDriverWrapper;

/**
 * Contains the configuration for accessing the DNA database.
 */
public class DatabaseConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConfiguration.class);

    private final String _hostPort;
    private final String _schemaName;
    private final String _userName;
    private final String _password;
    private final JDBCDriverWrapper _driverMapper;
    private final File _file;
    private final String _specimenQuery;
    private boolean _singleRowQuery;
    private int _specimenIdColumnIndex;
    private int _alleleColumnIndex;
    private int _locusColumnIndex;
    private final String _databaseRevisionQuery;
    private final String _specimenKeyQuery;

    /**
     * Constructor for describing a connection to a DNA database located in a DBMS.
     *
     * @param driverMapper the {@link JDBCDriverWrapper} for the JDBC driver
     * @param hostPort a String holding the host and port of the database instance
     * @param schemaName a String containing the schema name of the database
     * @param userName a String containing the username for logging in to the database
     * @param password a String containing the password for logging in to the database
     * @param specimenKeyQuery a String containing the query for getting a list of keys for eligible specimens
     * @param specimenQuery a String containing the query for getting the contents of one or more specimens from the database
     * @param databaseRevisionQuery a String containing the query for getting a value that identifies the current state of the database
     */
    public DatabaseConfiguration(final JDBCDriverWrapper driverMapper, final String hostPort, final String schemaName, final String userName, final String password, final String specimenKeyQuery, final String specimenQuery, final String databaseRevisionQuery) {
        _driverMapper = driverMapper;
        _hostPort = hostPort;
        _schemaName = schemaName;
        _userName = userName;
        _password = password;
        _specimenKeyQuery = specimenKeyQuery;
        _specimenQuery = specimenQuery;
        _databaseRevisionQuery = databaseRevisionQuery;
        _file = null;
    }

    /**
     * Constructor for describing a connection to a file-based DNA database.
     *
     * @param dbFile a {@link File} representing the file containing the DNA specimens
     */
    public DatabaseConfiguration(final File dbFile) {
        _hostPort = "";
        _schemaName = "";
        _userName = "";
        _password = "";
        _specimenKeyQuery = "";
        _specimenQuery = "";
        _databaseRevisionQuery = "";
        _driverMapper = null;
        _file = dbFile;
    }

    /**
     * @return the databaseType
     */
    public String getDatabaseType() {
        if (_driverMapper != null) {
            return _driverMapper.toString();
        }
        return "File";
    }

    /**
     * @return the host and port of the database server
     */
    public String getHostAndPort() {
        return _hostPort;
    }

    /**
     * @return the name of the schema
     */
    public String getSchemaName() {
        return _schemaName;
    }

    /**
     * @return the userName for logging in to the database
     */
    public String getUserName() {
        return _userName;
    }

    /**
     * @return the password for logging in to the database
     */
    public String getPassword() {
        return _password;
    }

    /**
     * @return the query for obtaining specimen records from the database
     */
    public String getSpecimenQuery() {
        return _specimenQuery;
    }

    /**
     * @return the query for obtaining a list of specimen keys from the database
     */
    public String getSpecimenKeyQuery() {
        return _specimenKeyQuery;
    }

    /**
     * @return the query for obtaining a value that identifies the current state of the database
     */
    public String getDatabaseRevisionQuery() {
        return _databaseRevisionQuery;
    }

    /**
     * Validates the database connection
     *
     * @throws SQLException if the database connection could not be set up.
     */
    public void validate() throws SQLException {
        final String jdbcUrl = _driverMapper.getConnectString(this);
        LOG.debug("Validating connection to {}", jdbcUrl);
        final Connection con = DriverManager.getConnection(jdbcUrl, _driverMapper.getProperties(this));
        con.close();
    }

    /**
     * Get the connection string for the database. For a JDBC-based database, this will be the JDBC connect string.
     * For a file-base database this will be the absolute file path.
     *
     * @return a String containing a descriptor for the database connection
     */
    public String getConnectString() {
        if (_driverMapper != null)
            return _driverMapper.getConnectString(this);
        return _file.getAbsolutePath();
    }

    /**
     * Sets whether the specimen query returns all specimen data in a single row or distributed over multiple rows.
     *
     * @param singleRowQuery true if the specimen query returns specimen data in a single row.
     */
    public void setSingleRowQuery(final boolean singleRowQuery) {
        _singleRowQuery = singleRowQuery;
    }

    /**
     * Indicates whether the specimen query returns all specimen data in a single row or distributed over multiple rows.
     *
     * @return true if the specimen query returns specimen data in a single row, false if the query returns this data distributed over multiple rows.
     */
    public boolean isSingleRowQuery() {
        return _singleRowQuery;
    }

    /**
     * Gets a query that can be used to test the database connection.
     *
     * @return a String containing the test query. Will be null for file-based databases.
     */
    public String getConnectionTestQuery() {
        if (_driverMapper != null)
            return _driverMapper.getConnectionTestQuery();
        return null;
    }

    /**
     * Gets the index for the column in the resultset of the specimen query where the specimen ID can be found.
     *
     * @return the 1-based index of the column containing the specimen ID
     */
    public int getSpecimenIdColumnIndex() {
        return _specimenIdColumnIndex;
    }

    /**
     * Sets the index for the column in the resultset of the specimen query where the specimen ID can be found.
     *
     * @param index the 1-based index of the column containing the specimen ID
     */
    public void setSpecimenIdColumnIndex(final int index) {
        _specimenIdColumnIndex = index;
    }

    /**
     * Gets the index for the column in the resultset of the specimen query where the allele value can be found.
     *
     * @return the 1-based index of the column containing the specimen ID
     */
    public int getAlleleColumnIndex() {
        return _alleleColumnIndex;
    }

    /**
     * Sets the index for the column in the resultset of the specimen query where the allele value can be found.
     *
     * @param index the 1-based index of the column containing the allele value
     */
    public void setAlleleColumnIndex(final int index) {
        _alleleColumnIndex = index;
    }

    /**
     * Gets the index for the column in the resultset of the specimen query where the locus name can be found.
     *
     * @return the 1-based index of the column containing the locus name
     */
    public int getLocusColumnIndex() {
        return _locusColumnIndex;
    }

    /**
     * Sets the index for the column in the resultset of the specimen query where the locus name can be found.
     *
     * @param index the 1-based index of the column containing the locus name
     */
    public void setLocusColumnIndex(final int index) {
        _locusColumnIndex = index;
    }

    /**
     * Gets a {@link Properties} object containing the properties for connecting to the database.
     *
     * @return a {@link Properties} object containing the properties for connecting the the database. Will be null for file-based databases.
     */
    public Properties getProperties() {
        if (_driverMapper != null)
            return _driverMapper.getProperties(this);
        return null;
    }
}
