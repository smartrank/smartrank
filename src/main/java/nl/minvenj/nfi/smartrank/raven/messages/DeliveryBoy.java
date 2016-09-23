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
package nl.minvenj.nfi.smartrank.raven.messages;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.raven.messages.conversion.Conversions;

public class DeliveryBoy implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DeliveryBoy.class);
    private final Object _source;
    private final Subscription _subscription;
    private final RavenMessage<?> _message;
    private final AtomicInteger _outstandingMessages;

    public DeliveryBoy(final Object source, final Subscription subscription, final RavenMessage<?> message, final AtomicInteger outstandingMessages) {
        _source = source;
        _subscription = subscription;
        _message = message;
        _outstandingMessages = outstandingMessages;
    }

    @Override
    public void run() {
        final Method method = _subscription.getMethod();
        final Object instance = _subscription.getInstance();
        LOG.debug("Notifying from {} to {} data '{}'", (_source instanceof String ? _source : _source.getClass().getSimpleName()), _subscription, _message.get());
        try {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            method.setAccessible(true);
            switch (parameterTypes.length) {
                case 0:
                    method.invoke(instance);
                    break;
                case 1:
                    method.invoke(instance, Conversions.convert(_message, parameterTypes[0]));
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected number of arguments for " + instance.getClass().getName() + "." + method.getName() + ": " + method.getParameterTypes().length);
            }
        }
        catch (IllegalAccessException | IllegalArgumentException ex) {
            LOG.error("Error sending message: {}\nPayload: {}\n  Source: {}\n  Destination: {}", _message, "" + _message.get(), _source, instance.getClass().getName(), ex);
            MessageBus.getInstance().send(instance, new ErrorStringMessage(ex.getLocalizedMessage()));
        }
        catch (final ConcurrentModificationException ex) {
            LOG.error("Error sending message: {}\nPayload: {}\nSource: {}\nDestination: {}", _message, "" + _message.get(), _source, instance.getClass().getName(), ex);
            MessageBus.getInstance().send(instance, new ErrorStringMessage(ex.getLocalizedMessage()));
        }
        catch (final InvocationTargetException ex) {
            LOG.error("Error sending message: {}\nPayload: {}\nSource: {}\nDestination: {}", _message, "" + _message.get(), _source, instance.getClass().getName(), ex.getCause());
            MessageBus.getInstance().send(instance, new ErrorStringMessage(ex.getCause().getLocalizedMessage()));
        }
        catch (final Throwable ex) {
            LOG.error("Error sending message: {}\nPayload: {}\nSource: {}\nDestination: {}", _message, "" + _message.get(), _source, instance.getClass().getName(), ex);
            MessageBus.getInstance().send(instance, new ErrorStringMessage(ex.getLocalizedMessage()));
        }
        finally {
            LOG.debug("Outstanding messages: {}", _outstandingMessages.decrementAndGet());
        }
    }
}
