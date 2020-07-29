package io.dinject.controller;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a method that handles HTTP PUT requests.
 */
@Target(value=METHOD)
@Retention(value=RUNTIME)
@HttpMethod(value="PUT")
public @interface Put {
  String value() default "";
}
