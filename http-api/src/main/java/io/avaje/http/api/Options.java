package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method that handles HTTP OPTIONS requests.
 */
@Target(METHOD)
@Retention(RUNTIME)
@HttpMethod("OPTIONS")
public @interface Options {

  /** Specify the path. */
  String value() default "";

}
