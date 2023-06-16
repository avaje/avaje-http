package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method that handles HTTP DELETE requests.
 *
 * <pre>{@code
 * @Delete("{id}")
 * void delete(long id) {
 *
 *   ...
 * }
 *
 * }</pre>
 */
@Target(METHOD)
@Retention(RUNTIME)
@HttpMethod("DELETE")
public @interface Delete {

  /** Specify the path. */
  String value() default "";

}
