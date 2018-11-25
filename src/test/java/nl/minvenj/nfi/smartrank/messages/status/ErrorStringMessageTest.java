package nl.minvenj.nfi.smartrank.messages.status;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ErrorStringMessageTest {

    @Test
    public final void testErrorStringMessage() {
        final ErrorStringMessage msg = new ErrorStringMessage("Some Message");
        assertThat(msg.get(), equalTo("Some Message"));
    }

    @Test
    public final void testErrorStringMessageNull() {
        final ErrorStringMessage msg = new ErrorStringMessage(null);
        assertThat(msg.get(), nullValue());
    }

}
