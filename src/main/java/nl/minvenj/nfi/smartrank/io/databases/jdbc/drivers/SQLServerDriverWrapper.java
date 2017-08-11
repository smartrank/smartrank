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
