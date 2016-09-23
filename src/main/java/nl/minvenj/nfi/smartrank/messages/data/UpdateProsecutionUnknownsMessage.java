package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class UpdateProsecutionUnknownsMessage extends RavenMessage<Number[]> {

    public UpdateProsecutionUnknownsMessage(final int unknowns, final double dropout) {
        super(new Number[]{unknowns, dropout});
    }

}
