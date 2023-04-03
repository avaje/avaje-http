package io.avaje.http.api;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a controller method parameter to be a body. Avaje can autodetect when a parameter is a
 * body, so this annotation is currently only for marking a string as a body parameter(for sending/receiving application/text or similar string input).
 */
@Retention(SOURCE)
@Target(PARAMETER)
public @interface Body {}
