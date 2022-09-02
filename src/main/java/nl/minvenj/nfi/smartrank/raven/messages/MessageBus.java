package nl.minvenj.nfi.smartrank.raven.messages;

import java.awt.EventQueue;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.raven.annotations.ExecuteOnSwingEventThread;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;

public class MessageBus {

    private static final Logger LOG = LoggerFactory.getLogger(MessageBus.class);

    private static MessageBus _me;

    private final HashMap<Class<? extends RavenMessage<?>>, CurrentValue> _currentValues;
    private final ReentrantReadWriteLock _lock;
    private final ExecutorService _notificationService;
    private final HashMap<Class<? extends RavenMessage<?>>, ArrayList<Subscription>> _subscriptions;
    private final MessageQueue _messageQueue;
    private final MessagePump _messagePump;

    public static MessageBus getInstance() {
        if (_me == null) {
            _me = new MessageBus();
        }
        return _me;
    }

    public static void reset() {
        // Stop the message pump
        if (_me != null) {
            _me._messagePump.interrupt();
            try {
                // Wait for the message pump to finish
                _me._messagePump.join();
            }
            catch (final InterruptedException e) {
                // Don't care if we were interrupted
            }
            _me._notificationService.shutdown();
            _me = null;
        }
    }

    private class MessageQueue {
        ArrayList<PendingMessage> _messages;

        public MessageQueue(final int capacity) {
            _messages = new ArrayList<MessageBus.PendingMessage>(capacity);
        }

        public PendingMessage take() throws InterruptedException {
            Thread.sleep(10);
            while (_messages.size() == 0) {
                Thread.sleep(10);
            }

            synchronized (_messages) {
                return _messages.remove(0);
            }
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public void put(final PendingMessage pendingMessage) {
            synchronized (_messages) {
                for (int idx = 0; idx < _messages.size(); idx++) {
                    final PendingMessage msg = _messages.get(idx);
                    if (msg.getMessage().getClass() == pendingMessage.getMessage().getClass()) {
                        if (pendingMessage.getMessage().isCoalescingEnabled()) {
                            final List msgList = (List) msg.getMessage().get();
                            final List pendingList = (List) pendingMessage.getMessage().get();
                            for (final Object o : pendingList) {
                                msgList.add(o);
                            }
                        }
                        else {
                            _messages.remove(idx);
                            _messages.add(idx, pendingMessage);
                        }
                        return;
                    }
                }
                _messages.add(pendingMessage);
            }
        }

        public boolean isEmpty() {
            synchronized (_messages) {
                return _messages.isEmpty();
            }
        }

    }

    private class CurrentValue {
        private final Object _source;
        private final long _timeSet;
        private final RavenMessage<?> _message;

        public CurrentValue(final Object source, final RavenMessage<?> message) {
            this(source, System.nanoTime(), message);
        }

        public CurrentValue(final Object source, final long timeSet, final RavenMessage<?> message) {
            _source = source;
            _timeSet = timeSet;
            _message = message;
        }

        public Object getSource() {
            return _source;
        }

        @SuppressWarnings("unused")
        public long getTimeSet() {
            return _timeSet;
        }

        public RavenMessage<?> getMessage() {
            return _message;
        }
    }

    private class PendingMessage {
        private final Object _source;
        private final RavenMessage<?> _message;

        public PendingMessage(final Object source, final RavenMessage<?> message) {
            _source = source;
            _message = message;
        }

        public Object getSource() {
            return _source;
        }

        public RavenMessage<?> getMessage() {
            return _message;
        }

        @Override
        public String toString() {
            return (_source instanceof String ? _source : _source.getClass().getSimpleName()) + ":" + _message.getClass().getSimpleName() + "(" + _message.get().toString() + ")";
        }
    }

    private class MessagePump extends Thread {

        AtomicInteger _outstandingMessages = new AtomicInteger();

        public MessagePump() {
            setName("MessagePump");
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    final PendingMessage pendingMessage = _messageQueue.take();
                    try {
                        _lock.readLock().lock();
                        final ArrayList<Subscription> subscriptions = _subscriptions.get(pendingMessage.getMessage().getClass());
                        if (subscriptions != null) {
                            for (final Subscription subscription : subscriptions) {
                                try {
                                    _outstandingMessages.incrementAndGet();
                                    if (subscription.executeOnEDT()) {
                                        EventQueue.invokeLater(new DeliveryBoy(pendingMessage.getSource(), subscription, pendingMessage.getMessage(), _outstandingMessages));
                                    }
                                    else {
                                        _notificationService.submit(new DeliveryBoy(pendingMessage.getSource(), subscription, pendingMessage.getMessage(), _outstandingMessages));
                                    }
                                }
                                catch (final Throwable t) {
                                    LOG.error("Error Dispatching {} from {} to {}", pendingMessage.getMessage().get(), (pendingMessage.getSource() instanceof String ? pendingMessage.getSource() : pendingMessage.getSource().getClass().getSimpleName()), subscription, t);
                                }
                            }
                        }
                    }
                    finally {
                        _lock.readLock().unlock();
                    }
                }
            }
            catch (final InterruptedException e) {
                LOG.info("Message pump interrupted!");
            }
            catch (final Throwable t) {
                LOG.info("Exception in message pump!", t);
            }
            LOG.info("Message pump stopped");
        }

        public void waitIdle(final long toWait) {
            final long start = System.currentTimeMillis();
            while ((System.currentTimeMillis() - start < toWait) && !_messageQueue.isEmpty() && _outstandingMessages.get() > 0) {
            }
        }
    }

    private MessageBus() {
        _currentValues = new HashMap<Class<? extends RavenMessage<?>>, CurrentValue>();
        _lock = new ReentrantReadWriteLock();
        _notificationService = Executors.newSingleThreadExecutor();
        _messageQueue = new MessageQueue(100);
        _subscriptions = new HashMap<Class<? extends RavenMessage<?>>, ArrayList<Subscription>>();
        _messagePump = new MessagePump();
        _messagePump.start();
    }

    /**
     * Registers the annotated subscription methods in the supplied class.
     *
     * @param instance
     * @throws InterruptedException
     */
    public void registerSubscriber(final Object instance) {
        _lock.writeLock().lock();
        try {
            Class<?> instanceClass = instance.getClass();
            while (instanceClass != null) {
                for (final Method m : instanceClass.getDeclaredMethods()) {
                    final RavenMessageHandler dch = m.getAnnotation(RavenMessageHandler.class);
                    if (dch != null) {
                        final ExecuteOnSwingEventThread eoset = m.getAnnotation(ExecuteOnSwingEventThread.class);
                        final Class<? extends RavenMessage<?>>[] messages = dch.value();
                        for (final Class<? extends RavenMessage<?>> messageClass : messages) {
                            addMessageSubscription(instance, m, eoset, messageClass);

                            // Notify then new subscriber of any value already set
                            final CurrentValue currentValue = _currentValues.get(messageClass);
                            if (currentValue != null) {
                                _messageQueue.put(new PendingMessage(currentValue.getSource(), currentValue.getMessage()));
                            }
                        }
                    }
                }
                instanceClass = instanceClass.getSuperclass();
            }
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    private void addMessageSubscription(final Object instance, final Method m, final ExecuteOnSwingEventThread eoset, final Class<? extends RavenMessage<?>> messageClass) {
        ArrayList<Subscription> subscriptions = _subscriptions.get(messageClass);
        if (subscriptions == null) {
            subscriptions = new ArrayList<Subscription>();
            _subscriptions.put(messageClass, subscriptions);
        }

        final Subscription subscription = new Subscription(instance, m, eoset != null);
        if (!subscriptions.contains(subscription)) {
            subscriptions.add(subscription);
        }
    }

    /**
     * Sends a message to the subscribed methods.
     *
     * @param source
     * @param message
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    public void send(final Object source, final RavenMessage<?> message) {
        _lock.writeLock().lock();
        try {
            if (message.isWaitForIdleBus())
                waitIdle(3000);
            final CurrentValue newValue = new CurrentValue(source, message);
            _currentValues.put((Class<? extends RavenMessage<?>>) message.getClass(), newValue);
            _messageQueue.put(new PendingMessage(source, message));
        }
        catch (final Throwable t) {
            LOG.error("Error sending message! sender={}, message={}", source instanceof String ? source : source.getClass().getSimpleName(), message, t);
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Queries the current value of the supplied message class.
     *
     * @param subjectClass
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T>T query(final Class<? extends RavenMessage<T>> subjectClass) {
        _lock.readLock().lock();
        try {
            final CurrentValue currentValue = _currentValues.get(subjectClass);
            if (currentValue != null)
                return (T) currentValue.getMessage().get();
            LOG.info("No value available for {} on messagebus {}", subjectClass.getSimpleName(), this);
            return null;
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    /**
     * Waits for the message bus to be idle, i.e. without pending messages or running notification threads.
     *
     * @param toWait the number of milliseconds to wait. The method will return when the message bus is idle or when the set time has expired, whichever comes first.
     */
    public void waitIdle(final long toWait) {
        _messagePump.waitIdle(toWait);
    }
}
