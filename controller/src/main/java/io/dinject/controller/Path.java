package io.dinject.controller;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specify the path mapping request to the controller.
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
 *
 * <h4>JAX-RS note</h4>
 * <p>
 * Note that unlike JAX-RS we only use <code>@Path</code> on the controller type and don't
 * use it on the methods. This is because the <code>@Get, @Post etc</code> annotations
 * include a path as well.
 * </p>
 */
@Target(value=TYPE)
@Retention(value=RUNTIME)
public @interface Path {
  String value();
}
