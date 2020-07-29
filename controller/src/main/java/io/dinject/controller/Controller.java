package io.dinject.controller;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marker annotation for controllers.
 *
 * <pre>{@code
 *
 *  @Controller
 *  @Path("/customers")
 *  class CustomerController {
 *    ...
 *  }
 *
 * }</pre>
 */
@Target(value=TYPE)
@Retention(value=RUNTIME)
public @interface Controller {
}
