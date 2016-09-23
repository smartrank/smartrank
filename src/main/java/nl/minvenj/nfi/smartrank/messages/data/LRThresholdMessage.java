package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class LRThresholdMessage extends RavenMessage<Integer> {

    public LRThresholdMessage(final Integer payload) {
        super(payload);
    }
}
