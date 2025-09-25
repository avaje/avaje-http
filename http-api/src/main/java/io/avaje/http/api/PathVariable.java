package io.avaje.http.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Marks a method parameter to be a path variable. */
@Retention(RUNTIME)
@Target({PARAMETER, FIELD})
public @interface PathVariable {

  /**
   * The name of the path variable.
   *
   * <p>If left blank the method parameter name is used.
   */
  String value();
}
