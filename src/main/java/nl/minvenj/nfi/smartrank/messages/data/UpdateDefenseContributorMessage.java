package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.domain.Contributor;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class UpdateDefenseContributorMessage extends RavenMessage<Contributor> {

    public UpdateDefenseContributorMessage(final Contributor payload) {
        super(payload);
    }

}
