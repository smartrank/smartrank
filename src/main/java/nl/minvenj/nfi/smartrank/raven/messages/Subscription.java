package nl.minvenj.nfi.smartrank.raven.messages;

import java.lang.reflect.Method;

public class Subscription {
    private final Object _instance;
    private final Method _method;
    private final boolean _executeOnSwingEventThread;

    public Subscription(final Object instance, final Method method, final boolean onEDT) {
        _instance = instance;
        _method = method;
        _executeOnSwingEventThread = onEDT;
    }

    public Object getInstance() {
        return _instance;
    }

    public Method getMethod() {
        return _method;
    }

    @Override
    public String toString() {
        return _instance.getClass().getSimpleName() + "." + _method.getName() + (_executeOnSwingEventThread ? "(on EDT)" : "");
    }

    boolean executeOnEDT() {
        return _executeOnSwingEventThread;
    }
}
