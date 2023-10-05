package io.avaje.http.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A parameter that is a header value.
 * <p>
 * We can put this on a method parameter or a <code>@Form</code> bean property.
 * </p>
 * <p>
 * By default header names are Init caps snake case. For example:
 * </p>
 * <pre>{@code
 *
 *  // Last-Modified
 *  @Header lastModified
 *
 * }</pre>
 */
@Target({PARAMETER,FIELD})
@Retention(RUNTIME)
public @interface Header {

  /**
   * The name of the header.
   */
  String value() default "";

}
