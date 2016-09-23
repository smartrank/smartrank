package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.domain.Contributor;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class UpdateDefenseNoncontributorMessage extends RavenMessage<Contributor> {

    public UpdateDefenseNoncontributorMessage(final Contributor payload) {
        super(payload);
    }

}
