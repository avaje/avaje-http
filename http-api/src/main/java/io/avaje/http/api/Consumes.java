package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specify endpoint request media type for the generated OpenAPI json.
 *
 * <p>When not specified the default MediaType is application/json, so we specify this on
 * controllers or methods where the responses return a different media type.
 *
 * <pre>{@code
 * @Path("/customers")
 * @Consumes(MediaType.TEXT_PLAIN)
 * class CustomerController {
 *   ...
 * }
 *
 * }</pre>
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface Consumes {

  /**
   * Specify request media type.
   *
   * <p>When not specified the default MediaType is application/json
   */
  String value() default MediaType.APPLICATION_JSON;
}
