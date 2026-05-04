package io.avaje.http.api;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a controller string method parameter to be a string body.
 * <p>
 * Use when you expect to receive an application/text or similar body request.
 * </p>
 *
 * @deprecated Use {@link Body} instead. Migrate existing {@code @BodyString String} parameters to
 *     {@code @Body String}.
 */
@Deprecated(since = "3.9")
@Retention(SOURCE)
@Target(PARAMETER)
public @interface BodyString {}
