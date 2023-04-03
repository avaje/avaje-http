package io.avaje.http.api;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a controller string method parameter to be a string body. Use when you expect to recive an
 * application/text or similar body request
 */
@Retention(SOURCE)
@Target(PARAMETER)
public @interface BodyString {}
