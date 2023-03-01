package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * Overrides global request timeout for this endpoint.
 *
 * <pre>{@code
 * @Client
 * interface CustomerApi {
 *   @Get("/{id}")
 *   @RequestTimeout(value = 1, ChronoUnit.SECONDS)
 *   Customer getById(long id);
 * }
 *
 * }</pre>
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface RequestTimeout {
  long value();

  ChronoUnit chronoUnit() default ChronoUnit.MILLIS;
}
