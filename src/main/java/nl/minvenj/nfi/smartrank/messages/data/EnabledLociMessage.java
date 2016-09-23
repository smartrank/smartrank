package nl.minvenj.nfi.smartrank.messages.data;

import java.util.List;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class EnabledLociMessage extends RavenMessage<List<String>> {

    public EnabledLociMessage(final List<String> payload) {
        super(payload);
    }

}
