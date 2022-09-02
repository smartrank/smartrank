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
package nl.minvenj.nfi.smartrank.io.databases.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.messages.status.DetailStringMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class JDBCConnectionFactory {

    private static final Logger LOG = LoggerFactory.getLogger(JDBCConnectionFactory.class);

    /**
     * Connects to the database as defined in the supplied database configuration.
     *
     * @param config a {@link DatabaseConfiguration} containing the details for connecting to the database
     * @return a {@link Connection} representing the connection to the database
     * @throws SQLException if any problem occurs connecting to the database
     */
    public static Connection connect(final DatabaseConfiguration config) throws SQLException {
        LOG.info("Establishing new connection to {}", config.getConnectString());
        int connectionTry = 0;
        Throwable failure = null;
        final int maxRetry = SmartRankGUISettings.getDatabaseConnectionRetryCount();
        while (connectionTry++ < maxRetry) {
            try {
                final Connection connection = DriverManager.getConnection(config.getConnectString(), config.getProperties());
                connection.setReadOnly(true);
                connection.setAutoCommit(false);
                return connection;
            }
            catch (final Throwable t) {
                LOG.error("Connection to database failed on try #{} of {}: {} - {}", connectionTry, maxRetry, t.getClass().getSimpleName(), t.getMessage());
                MessageBus.getInstance().send("JDBCConnectionFactory", new DetailStringMessage("Connecting to database try " + connectionTry + " of " + maxRetry + " failed, waiting to retry"));
                failure = t;
                if (connectionTry < maxRetry) {
                    try {
                        Thread.sleep(SmartRankGUISettings.getDatabaseConnectionRetryTimeout());
                    }
                    catch (final InterruptedException e) {
                        throw new SQLException("Database connection failed for " + config.getConnectString(), e);
                    }
                }
            }
        }

        throw new SQLException("Database connection failed for " + config.getConnectString(), failure);
    }

}
