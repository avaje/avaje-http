package io.avaje.http.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marker annotation for controllers.
 *
 * <pre>{@code
 * @Controller("/customers")
 * class CustomerController {
 *   ...
 * }
 *
 * }</pre>
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Controller {

  /** Specify the path mapping request to the controller. */
  String value() default "";

  /**
   * Specify if the http request context should be instrumented via RequestContextResolver
   *
   * @deprecated use InstrumentServerContext annotation instead
   */
  @Deprecated
  boolean instrumentRequestContext() default false;
}
