package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method that handles HTTP POST requests.
 *
 * <pre>{@code
 *
 *  @Post
 *  void save(Customer customer) {
 *     ...
 *  }
 * }</pre>
 */
@Target(METHOD)
@Retention(RUNTIME)
@HttpMethod("POST")
public @interface Post {

  /** Specify the path. */
  String value() default "";

}
