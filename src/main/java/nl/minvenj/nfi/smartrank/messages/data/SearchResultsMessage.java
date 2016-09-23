package nl.minvenj.nfi.smartrank.messages.data;

import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class SearchResultsMessage extends RavenMessage<SearchResults> {

    public SearchResultsMessage(final SearchResults payload) {
        super(payload);
    }

}
