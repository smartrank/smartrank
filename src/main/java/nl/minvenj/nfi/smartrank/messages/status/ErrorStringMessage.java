package nl.minvenj.nfi.smartrank.messages.status;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class ErrorStringMessage extends RavenMessage<String> {

    public ErrorStringMessage(final String payload) {
        super(payload);
    }
}
