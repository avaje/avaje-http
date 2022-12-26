package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** a container for the OpenAPIReturns annotation to make it repeatable */
@Target(value = METHOD)
@Retention(value = RUNTIME)
public @interface OpenAPIReturnsContainer {
  OpenAPIReturns[] value();
}
