package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.domain.Contributor;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class UpdateProsecutionNoncontributorMessage extends RavenMessage<Contributor> {

    public UpdateProsecutionNoncontributorMessage(final Contributor payload) {
        super(payload);
    }

}
