package nl.minvenj.nfi.smartrank.messages.commands;

import java.util.List;

import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class UpdateCrimeSceneProfiles extends RavenMessage<List<Sample>> {

    public UpdateCrimeSceneProfiles(final List<Sample> payload) {
        super(payload, true);
    }
}
