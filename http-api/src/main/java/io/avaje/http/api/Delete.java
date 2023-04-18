package io.avaje.http.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a method that handles HTTP DELETE requests.
 *
 * <pre>{@code
 *
 *   @Delete("{id}")
 *   void delete(long id) {
 *
 *     ...
 *   }
 *
 * }</pre>
 */
@Target(METHOD)
@Retention(RUNTIME)
@HttpMethod("DELETE")
public @interface Delete {

  /** Specify the path. */
  String value() default "";

  boolean instrumentRequestContext() default false;
}
