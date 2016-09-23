package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class UpdateDefenseUnknownsMessage extends RavenMessage<Number[]> {

    public UpdateDefenseUnknownsMessage(final int unknowns, final double dropout) {
        super(new Number[]{unknowns, dropout});
    }

}
