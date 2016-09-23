package nl.minvenj.nfi.smartrank.messages.data;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class DatabaseFileMessage extends RavenMessage<List<File>> {

    public DatabaseFileMessage(final File payload) {
        this(Arrays.asList(payload));
    }

    public DatabaseFileMessage(final List<File> payload) {
        super(payload);
    }

}
