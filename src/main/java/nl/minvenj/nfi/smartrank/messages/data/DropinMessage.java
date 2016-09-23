package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class DropinMessage extends RavenMessage<Double> {

    public DropinMessage(final Double payload) {
        super(payload);
    }

}
