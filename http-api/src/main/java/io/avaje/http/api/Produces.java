package io.avaje.http.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specify endpoint response media type.
 *
 * When not specified the default MediaType is APPLICATION_JSON
 * so we specify this on controllers or methods where the responses
 * return a different media type.
 *
 * <pre>{@code
 *
 *  @Produces(MediaType.TEXT_PLAIN)
 *  @Path("/customers")
 *  class CustomerController {
 *    ...
 *  }
 *
 * }</pre>
 */
@Target(value = {TYPE, METHOD})
@Retention(value = RUNTIME)
public @interface Produces {

  /**
   * Specify response media type.
   *
   * <p>When not specified the default MediaType is APPLICATION_JSON
   */
  String value() default MediaType.APPLICATION_JSON;

  /**
   * The default status code of the generated route.
   *
   * <p>When not specified, the default status are as follows: <br>
   * GET(200) <br>
   * POST(201) <br>
   * PUT(200, void methods 204) <br>
   * PATCH(200, void methods 204) <br>
   * DELETE(200, void methods 204)
   */
  int defaultStatus() default 0;
}
