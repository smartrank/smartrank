package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class ThetaMessage extends RavenMessage<Double> {

    public ThetaMessage(final Double payload) {
        super(payload);
    }

}
