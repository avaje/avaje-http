package io.avaje.http.api;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Marks a method parameter to be a matrix parameter. */
@Target(value = {PARAMETER})
@Retention(value = RUNTIME)
public @interface MatrixParam {

  /**
   * The name of the matrix parameter.
   *
   * <p>If left blank the method parameter name is used.
   *
   * <p>We typically use this when the matrix parameter uses snake-case or similar that does not map
   * to a valid java/kotlin parameter name.
   */
  String value() default "";
}
