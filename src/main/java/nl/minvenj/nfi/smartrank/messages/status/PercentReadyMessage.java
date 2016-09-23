package nl.minvenj.nfi.smartrank.messages.status;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class PercentReadyMessage extends RavenMessage<Integer> {
    public PercentReadyMessage(final int percentReady) {
        super(percentReady);
    }
}
