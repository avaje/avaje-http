package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Marks a method that handles HTTP PATCH requests. */
@Target(METHOD)
@Retention(RUNTIME)
@HttpMethod("PATCH")
public @interface Patch {

  /** Specify the path. */
  String value() default "";
  /**
   * Specify if the http request context should be instrumented via RequestContextResolver
   *
   * @deprecated use InstrumentServerContext annotation instead
   */
  @Deprecated
  boolean instrumentRequestContext() default false;
}
