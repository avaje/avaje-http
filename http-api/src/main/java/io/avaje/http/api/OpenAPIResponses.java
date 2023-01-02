package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Container for repeatable {@link OpenAPIResponse} annotation
 *
 * @see OpenAPIResponse
 */
@Target(value = METHOD)
@Retention(value = RUNTIME)
public @interface OpenAPIResponses {
  OpenAPIResponse[] value();
}
