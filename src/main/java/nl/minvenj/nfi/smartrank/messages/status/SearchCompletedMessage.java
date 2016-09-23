package nl.minvenj.nfi.smartrank.messages.status;

import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class SearchCompletedMessage extends RavenMessage<SearchResults> {

    public SearchCompletedMessage(final SearchResults payload) {
        super(payload);
    }

}
