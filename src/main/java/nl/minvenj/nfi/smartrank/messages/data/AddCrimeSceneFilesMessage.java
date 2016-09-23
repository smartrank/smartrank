package nl.minvenj.nfi.smartrank.messages.data;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class AddCrimeSceneFilesMessage extends RavenMessage<List<File>> {

    public AddCrimeSceneFilesMessage(final File payload) {
        super(Arrays.asList(payload));
    }

    public AddCrimeSceneFilesMessage(final List<File> payload) {
        super(payload);
    }

}
