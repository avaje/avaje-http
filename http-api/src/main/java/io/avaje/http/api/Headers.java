package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Headers for an Http <code>@Client</code> interface method.
 *
 * <p>We can put this on a method or the interface to add preset headers to the generated
 * implementation bean property.
 *
 * <pre>{@code
 * @Headers({
 * "Accept: application/vnd.github.v3.full+json",
 * "User-Agent: Avaje-Sample-App"
 * })
 * @Get("users/{username}")
 * User getUser(@Path("username") String username);
 *
 * }</pre>
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Headers {

  /** The array of headers */
  String[] value();
}
