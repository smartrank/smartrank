package nl.minvenj.nfi.smartrank.messages.commands;

import nl.minvenj.nfi.smartrank.io.WritableFileSource;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class WritableFileSourceMessage extends RavenMessage<WritableFileSource> {

    public WritableFileSourceMessage(final WritableFileSource payload) {
        super(payload);
    }
}
