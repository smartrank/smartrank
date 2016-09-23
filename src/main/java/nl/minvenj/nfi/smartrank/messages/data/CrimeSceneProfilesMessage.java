package nl.minvenj.nfi.smartrank.messages.data;

import java.util.List;

import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class CrimeSceneProfilesMessage extends RavenMessage<List<Sample>> {

    public CrimeSceneProfilesMessage(final List<Sample> payload) {
        super(payload);
    }
}
