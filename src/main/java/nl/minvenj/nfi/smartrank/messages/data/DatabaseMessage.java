package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.domain.DNADatabase;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class DatabaseMessage extends RavenMessage<DNADatabase> {

    public DatabaseMessage(final DNADatabase database) {
        super(database);
    }

}
