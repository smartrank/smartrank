package nl.minvenj.nfi.smartrank.raven.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RavenMessageHandler {

    Class<? extends RavenMessage<?>>[] value();
}
