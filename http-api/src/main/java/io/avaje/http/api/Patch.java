package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Marks a method that handles HTTP PATCH requests. */
@Target(value = METHOD)
@Retention(value = RUNTIME)
@HttpMethod(value = "PATCH")
public @interface Patch {
  String value() default "";
}
