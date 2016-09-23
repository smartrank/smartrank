package nl.minvenj.nfi.smartrank.messages.commands;

import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class UpdateKnownProfile extends RavenMessage<Sample> {

    public UpdateKnownProfile(final Sample payload) {
        super(payload);
    }
}
