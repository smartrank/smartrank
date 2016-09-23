package nl.minvenj.nfi.smartrank.raven;

import java.util.Collection;

public class NullUtils {

    public static <T>T getValue(final T value, final T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static int safeSize(final Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    public static <T>T argNotNull(final T object, final String objectName) {
        if (object == null)
            throw new IllegalArgumentException(objectName + " must not be null!");
        return object;
    }

}
