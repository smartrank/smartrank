package nl.minvenj.nfi.smartrank.messages.data;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class PopulationStatisticsFileMessage extends RavenMessage<List<File>> {

    public PopulationStatisticsFileMessage(final File payload) {
        this(Arrays.asList(payload));
    }

    public PopulationStatisticsFileMessage(final List<File> payload) {
        super(payload);
    }
}
