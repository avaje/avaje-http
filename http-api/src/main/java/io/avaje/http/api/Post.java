package io.avaje.http.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a method that handles HTTP POST requests.
 *
 * <pre>{@code
 *
 *  @Post
 *  void save(Customer customer) {
      ...
 *  }
 */
@Target(METHOD)
@Retention(RUNTIME)
@HttpMethod("POST")
public @interface Post {

  /** Specify the path. */
  String value() default "";

  boolean instrumentRequestContext() default false;
}
