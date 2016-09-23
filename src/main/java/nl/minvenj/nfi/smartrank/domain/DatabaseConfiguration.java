package nl.minvenj.nfi.smartrank.domain;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.io.databases.jdbc.JDBCDriverWrapper;

public class DatabaseConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConfiguration.class);

    private final String _hostPort;
    private final String _schemaName;
    private final String _userName;
    private final String _password;
    private final JDBCDriverWrapper _driverMapper;
    private final File _file;
    private final String _query;
    private boolean _singleRowQuery;
    private int _specimenIdColumnIndex;
    private int _alleleColumnIndex;
    private int _locusColumnIndex;

    public DatabaseConfiguration(final JDBCDriverWrapper driverMapper, final String hostPort, final String schemaName, final String userName, final String password, final String query) {
        _driverMapper = driverMapper;
        _hostPort = hostPort;
        _schemaName = schemaName;
        _userName = userName;
        _password = password;
        _query = query;
        _file = null;
    }

    public DatabaseConfiguration(final File dbFile) {
        _hostPort = "";
        _schemaName = "";
        _userName = "";
        _password = "";
        _query = "";
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
    public String getQuery() {
        return _query;
    }

    public void validate() throws SQLException {
        final String jdbcUrl = _driverMapper.getConnectString(this);
        LOG.debug("Validating connection to {}", jdbcUrl);
        final Connection con = DriverManager.getConnection(jdbcUrl, _userName, _password);
        con.close();
    }

    public String getConnectString() {
        if (_driverMapper != null)
            return _driverMapper.getConnectString(this);
        return _file.getAbsolutePath();
    }

    public void setSingleRowQuery(final boolean singleRowQuery) {
        _singleRowQuery = singleRowQuery;
    }

    public boolean isSingleRowQuery() {
        return _singleRowQuery;
    }

    public String getResultSizeQuery() {
        if (_driverMapper != null)
            return _driverMapper.getResultSizeQuery(_query);
        return null;
    }

    public int getSpecimenIdColumnIndex() {
        return _specimenIdColumnIndex;
    }

    public void setSpecimenIdColumnIndex(final int index) {
        _specimenIdColumnIndex = index;
    }

    public int getAlleleColumnIndex() {
        return _alleleColumnIndex;
    }

    public void setAlleleColumnIndex(final int index) {
        _alleleColumnIndex = index;
    }

    public int getLocusColumnIndex() {
        return _locusColumnIndex;
    }

    public void setLocusColumnIndex(final int index) {
        _locusColumnIndex = index;
    }
}
