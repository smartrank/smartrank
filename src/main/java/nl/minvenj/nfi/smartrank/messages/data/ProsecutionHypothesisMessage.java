package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class ProsecutionHypothesisMessage extends RavenMessage<ProsecutionHypothesis> {

    public ProsecutionHypothesisMessage(final ProsecutionHypothesis payload) {
        super(payload);
    }

}
