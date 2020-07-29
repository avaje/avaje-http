package io.dinject.controller;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a method that handles HTTP DELETE requests.
 *
 * <pre>{@code
 *
 *   @Delete(":id")
 *   void delete(long id) {
 *
 *     ...
 *   }
 *
 * }</pre>
 */
@Target(value=METHOD)
@Retention(value=RUNTIME)
@HttpMethod(value="DELETE")
public @interface Delete {

  /**
   * Specify the path.
   */
  String value() default "";
}
