package nl.minvenj.nfi.smartrank.messages.status;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class DetailStringMessage extends RavenMessage<String> {

    public DetailStringMessage(final String payload) {
        super(payload);
    }
}
