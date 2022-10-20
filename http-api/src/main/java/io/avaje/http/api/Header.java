package io.avaje.http.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * A parameter that is a header value.
 *
 * <p>We can put this on a method parameter or a <code>@Form</code> bean property.
 *
 * <p>By default header names are Init caps snake case. For example:
 *
 * <pre>{@code
 * // Last-Modified
 * @Header lastModified
 *
 * }</pre>
 */
@Target(value = {PARAMETER, FIELD})
@Retention(value = RUNTIME)
public @interface Header {

  /** The name of the header. */
  String value() default "";
}
