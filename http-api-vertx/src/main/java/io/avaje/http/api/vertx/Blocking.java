package io.avaje.http.api.vertx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to indicate that the http method is blocking and should be executed on a worker
 * thread rather than the event loop thread. This is important for methods that perform blocking
 * operations such as database access, file I/O, or any long-running computations.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Blocking {}
