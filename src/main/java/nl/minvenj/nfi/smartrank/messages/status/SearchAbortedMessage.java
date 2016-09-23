package nl.minvenj.nfi.smartrank.messages.status;

import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class SearchAbortedMessage extends RavenMessage<SearchResults> {

    public SearchAbortedMessage(final SearchResults payload) {
        super(payload);
    }
}
