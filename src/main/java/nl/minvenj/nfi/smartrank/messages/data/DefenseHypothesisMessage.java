package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class DefenseHypothesisMessage extends RavenMessage<DefenseHypothesis> {

    public DefenseHypothesisMessage(final DefenseHypothesis payload) {
        super(payload);
    }

}
