package io.avaje.http.javalin;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method that handles Javalin before requests.
 *
 * <pre>{@code
 *
 *  @Before
 *  void save(Customer customer) {
 * ...
 *  }
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Before {}
