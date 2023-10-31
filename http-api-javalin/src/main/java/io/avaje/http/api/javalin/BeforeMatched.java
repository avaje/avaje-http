package io.avaje.http.api.javalin;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method that handles Javalin beforeMatched requests.
 *
 * <pre>{@code
 *
 *  @BeforeMatched
 *  void save(Customer customer) {
 *    ...
 *  }
 *
 * }</pre>
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface BeforeMatched {}
