package nl.minvenj.nfi.smartrank.messages.status;

import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class ApplicationStatusMessage extends RavenMessage<ApplicationStatus> {
    public ApplicationStatusMessage(final ApplicationStatus payload) {
        super(payload);
    }
}
