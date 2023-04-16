package io.avaje.http.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a method that handles HTTP GET requests.
 *
 * <pre>{@code
 *
 *   @Get("{id}")
 *   Customer get(long id) {
 *
 *     ...
 *   }
 *
 * }</pre>
 *
 * <h4>Example</h4>
 * <p>
 * Path parameters are matched by name - "status" in the example below.
 * </p>
 * <p>
 * Method parameters that do not match a path parameter default to being
 * a query parameter - "since" is a query parameter in the example below.
 * </p>
 *
 * <pre>{@code
 *
 *   @Get("/status/{status}")
 *   List<Customer> getByStatus(String status, LocalDate since) {
 *
 *     ...
 *   }
 *
 * }</pre>
 *
 * <h4>Example - Multiple path parameters</h4>
 * <pre>{@code
 *
 *   @Get("/status/{status}/{parentId}")
 *   List<Customer> getByStatus(String status, long parentId, LocalDate since) {
 *
 *     ...
 *   }
 *
 * }</pre>
 */
@Target(METHOD)
@Retention(RUNTIME)
@HttpMethod("GET")
public @interface Get {

  /** Specify the path. */
  String value() default "";

  boolean instrumentRequestContext() default false;
}
