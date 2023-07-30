package io.avaje.http.helidon;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method as a Helidon Filter
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Filter {}
