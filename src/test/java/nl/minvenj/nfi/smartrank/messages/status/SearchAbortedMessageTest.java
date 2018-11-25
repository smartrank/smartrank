package nl.minvenj.nfi.smartrank.messages.status;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import nl.minvenj.nfi.smartrank.analysis.SearchResults;

public class SearchAbortedMessageTest {

    @Test
    public final void testSearchAbortedMessage() {
        final SearchResults results = new SearchResults(0, null);
        final SearchAbortedMessage msg = new SearchAbortedMessage(results);
        assertThat(msg.get(), is(results));
    }

    @Test
    public final void testSearchAbortedMessageNull() {
        final SearchAbortedMessage msg = new SearchAbortedMessage(null);
        assertThat(msg.get(), nullValue());
    }

}
