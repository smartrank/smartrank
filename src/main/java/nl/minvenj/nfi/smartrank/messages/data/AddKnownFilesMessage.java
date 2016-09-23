package nl.minvenj.nfi.smartrank.messages.data;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class AddKnownFilesMessage extends RavenMessage<List<File>> {

    public AddKnownFilesMessage(final File payload) {
        this(Arrays.asList(payload));
    }

    public AddKnownFilesMessage(final List<File> payload) {
        super(payload);
    }

}
