package nl.minvenj.nfi.smartrank.messages.data;

import java.util.Arrays;
import java.util.List;

import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class KnownProfilesMessage extends RavenMessage<List<Sample>> {

    public KnownProfilesMessage(final Sample payload) {
        this(Arrays.asList(payload));
    }

    public KnownProfilesMessage(final List<Sample> payload) {
        super(payload);
    }
}
