package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specify endpoint response media type.
 *
 * <p>When not specified the default MediaType is APPLICATION_JSON so we specify this on controllers
 * or methods where the responses return a different media type.
 *
 * <pre>{@code
 * @Produces(MediaType.TEXT_PLAIN)
 * @Path("/customers")
 * class CustomerController {
 *   ...
 * }
 *
 * }</pre>
 */
@Target(value = {TYPE, METHOD})
@Retention(value = RUNTIME)
public @interface Produces {

  String value();
}
