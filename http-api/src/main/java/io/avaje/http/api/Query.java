package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method that handles HTTP QUERY requests.
 *
 * <pre>{@code
 * @Query
 * List<Customer> search(SearchCriteria criteria) {
 *   ...
 * }
 * }</pre>
 */
@Target(METHOD)
@Retention(RUNTIME)
@HttpMethod("QUERY")
public @interface Query {

  /** Specify the path. */
  String value() default "";

}
