package nl.minvenj.nfi.smartrank.messages.data;

import java.util.List;

import nl.minvenj.nfi.smartrank.domain.LikelihoodRatio;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class LikelihoodRatiosMessage extends RavenMessage<List<LikelihoodRatio>> {

    public LikelihoodRatiosMessage(final List<LikelihoodRatio> payload) {
        super(payload);
    }
}
