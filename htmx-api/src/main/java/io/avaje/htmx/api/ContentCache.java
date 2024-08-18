package io.avaje.htmx.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Mark a controller method as using a content cache.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface ContentCache {

}
