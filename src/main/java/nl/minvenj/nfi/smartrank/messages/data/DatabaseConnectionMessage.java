package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class DatabaseConnectionMessage extends RavenMessage<DatabaseConfiguration> {

    public DatabaseConnectionMessage(final DatabaseConfiguration payload) {
        super(payload);
    }

}
