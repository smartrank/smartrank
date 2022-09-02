package nl.minvenj.nfi.smartrank.messages.data;

import java.io.File;
import java.util.List;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class LoadSearchCriteriaMessage extends RavenMessage<List<File>> {

    public LoadSearchCriteriaMessage(final List<File> payload) {
        super(payload);
    }

}
