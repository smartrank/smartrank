package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.domain.Contributor;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class UpdateProsecutionContributorMessage extends RavenMessage<Contributor> {

    public UpdateProsecutionContributorMessage(final Contributor payload) {
        super(payload);
    }

}
