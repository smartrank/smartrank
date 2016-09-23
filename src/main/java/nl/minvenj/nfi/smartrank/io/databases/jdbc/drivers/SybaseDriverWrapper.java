package nl.minvenj.nfi.smartrank.io.databases.jdbc.drivers;

import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.io.databases.jdbc.JDBCDriverWrapper;

public class SybaseDriverWrapper implements JDBCDriverWrapper {

    @Override
    public String getConnectString(final DatabaseConfiguration databaseConfiguration) {
        return "jdbc:jtds:sybase://" + databaseConfiguration.getHostAndPort() + "/" + databaseConfiguration.getSchemaName();
    }

    @Override
    public String toString() {
        return "Sybase";
    }

    @Override
    public String getResultSizeQuery(final String query) {
        return "SELECT count(*) from (" + query + ") as countAlias";
    }
}
