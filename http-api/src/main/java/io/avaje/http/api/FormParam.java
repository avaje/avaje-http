package io.avaje.http.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Marks a method parameter to be a form parameter. */
@Target(value = {PARAMETER, FIELD})
@Retention(value = RUNTIME)
public @interface FormParam {

  /**
   * The name of the form parameter.
   *
   * <p>If left blank the method parameter name is used.
   */
  String value() default "";
}
