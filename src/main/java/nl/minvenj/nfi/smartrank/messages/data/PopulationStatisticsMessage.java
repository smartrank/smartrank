package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class PopulationStatisticsMessage extends RavenMessage<PopulationStatistics> {

    public PopulationStatisticsMessage(final PopulationStatistics payload) {
        super(payload);
    }

}
