package nl.minvenj.nfi.smartrank.raven.messages;

import java.util.List;

/**
 * A base class for the messages that can be sent on the {@link MessageBus}.
 *
 * @param <T> the payload object type to be returned by the {@link RavenMessage#get()} method
 */
public class RavenMessage<T> {

    private final T _payload;
    private boolean _coalescingEnabled;
    private String _warningMessage;

    /**
     * Constructor for messages that can not be coalesced by the {@link MessageBus}.
     *
     * @param payload the payload object
     */
    public RavenMessage(final T payload) {
        this(payload, false);
    }

    /**
     * General purpose constructor.
     *
     * @param payload the payload object
     * @param coalescingEnabled <b>true</b> if the {@link MessageBus} can coalesce the data of multiple instances of this message into a single message
     */
    public RavenMessage(final T payload, final boolean coalescingEnabled) {
        if (coalescingEnabled && !(payload instanceof List))
            throw new IllegalArgumentException("payload must be a List if coalescing is enabled!");
        _payload = payload;
        _coalescingEnabled = coalescingEnabled;
        _warningMessage = null;
    }

    /**
     * Getter for the payload.
     *
     * @return the payload object supplied in the constructor
     */
    public T get() {
        return _payload;
    }

    /**
     * Indicates if the {@link MessageBus} should coalesce the data for multiple instances of this message into a single message.
     *
     * @return <b>true</b> if this message should be coalesced
     */
    public boolean isCoalescingEnabled() {
        return _coalescingEnabled;
    }

    public boolean isWaitForIdleBus() {
        return false;
    }

    /**
     * @return the warningMessage
     */
    public String getWarningMessage() {
        return _warningMessage;
    }

    /**
     * @param warningMessage the warningMessage to set
     */
    public void setWarningMessage(final String warningMessage) {
        _warningMessage = warningMessage;
    }
}
