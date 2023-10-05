package io.avaje.http.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a method parameter to be a form parameter.
 */
@Target({PARAMETER,FIELD})
@Retention(RUNTIME)
public @interface FormParam {

  /**
   * The name of the form parameter.
   * <p>
   * If left blank the method parameter name is used.
   * </p>
   */
  String value() default "";
}
