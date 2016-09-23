package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class AnalysisParametersMessage extends RavenMessage<AnalysisParameters> {

    public AnalysisParametersMessage(final AnalysisParameters payload) {
        super(payload);
    }

}
