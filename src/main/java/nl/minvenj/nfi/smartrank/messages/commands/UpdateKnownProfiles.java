package nl.minvenj.nfi.smartrank.messages.commands;

import java.util.List;

import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class UpdateKnownProfiles extends RavenMessage<List<Sample>> {

    public UpdateKnownProfiles(final List<Sample> payload) {
        super(payload, true);
    }
}
