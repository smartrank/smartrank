package nl.minvenj.nfi.smartrank.messages.commands;

import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class UpdateCrimeSceneProfile extends RavenMessage<Sample> {

    public UpdateCrimeSceneProfile(final Sample payload) {
        super(payload);
    }
}
