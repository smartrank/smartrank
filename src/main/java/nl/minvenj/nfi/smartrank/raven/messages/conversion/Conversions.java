/*
 * Copyright (C) 2015 Netherlands Forensic Institute
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.minvenj.nfi.smartrank.raven.messages.conversion;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class Conversions {

    private static final Logger LOG = LoggerFactory.getLogger(Conversions.class);

    private Conversions() {
    }

    public static Object convert(final Object input, final Class<?> outputType) {
        LOG.debug("convert({}:{}, {})", input == null ? null : input.getClass(), input, outputType);

        Object singleObject;

        // If required type is a ravenmessage, return the input
        if (input instanceof RavenMessage<?>) {
            if (outputType == input.getClass()) {
                return input;
            }
            // Conversion from an array of 1 to a single element
            singleObject = toSingleObject(((RavenMessage<?>) input).get(), outputType);
        }
        else {
            // Conversion from an array of 1 to a single element
            singleObject = toSingleObject(input, outputType);
        }

        Object result = null;

        // null stays null
        if (singleObject == null) {
            return null;
        }

        // autoboxing-type stuff
        if (outputType.isPrimitive() || outputType.isAssignableFrom(singleObject.getClass())) {
            result = singleObject;
        }

        // conversion to string
        if (result == null && String.class.equals(outputType)) {
            result = singleObject.toString();
        }

        // conversion to File
        if (result == null && outputType.isAssignableFrom(File.class)) {
            result = new File(singleObject.toString());
        }

        if (result != null) {
            result = toArray(result, outputType);
        } else {
                result = toArray(singleObject, outputType);
        }
        if (result != null) {
            LOG.debug("conversion result: {}:{}", result.getClass(), result);
            return result;
        }

        throw new IllegalArgumentException(String.format("Conversion error: %s cannot be converted to %s for %s", input.getClass(), outputType, input));
    }

    private static Object toSingleObject(final Object input, final Class<?> outputType) {
        if (input == null) {
            return null;
        }

        if (outputType.isArray() || Collection.class.isAssignableFrom(outputType)) {
            return input;
        }

        if (input.getClass().isArray()) {
            if (Array.getLength(input) == 1) {
                return Array.get(input, 0);
            }
            if (Array.getLength(input) == 0) {
                return null;
            }
        } else {
            if (input instanceof Collection) {
                final Collection<?> coll = (Collection<?>) input;
                if (coll.size() == 1) {
                    return coll.iterator().next();
                }
                if (coll.isEmpty()) {
                    return null;
                }
            }
        }

        return input;
    }

    private static Object toArray(final Object input, final Class<?> outputType) {
        LOG.debug("toArray({},{})", input, outputType);
        if (outputType.isArray()) {
            LOG.debug("outputType.isArray()");
            if (input instanceof Collection) {
                LOG.debug("input instanceof Collection");
                final Collection<?> coll = (Collection<?>) input;
                final Object retval = Array.newInstance(outputType.getComponentType(), coll.size());
                int idx = 0;
                final Iterator<?> i = coll.iterator();
                while (i.hasNext()) {
                    final Object current = i.next();
                    Array.set(retval, idx++, convert(current, outputType.getComponentType()));
                }
                return retval;
            }
            if (!input.getClass().isArray()) {
                LOG.debug("!input.getClass().isArray()");
                final Object retval = Array.newInstance(outputType.getComponentType(), 1);
                Array.set(retval, 0, convert(input, outputType.getComponentType()));
                return retval;
            }
        }

        LOG.debug("return {}", input);
        return input;
    }
}
