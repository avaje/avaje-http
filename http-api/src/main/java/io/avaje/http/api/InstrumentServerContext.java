package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a controller method to be instrumented with <code>RequestContextResolver</code>.
 *
 * <p>When instrumented,<code>resolver.currentRequest()</code> can be used to retrieve the current request during a handler method execution
 *
 * <pre>{@code
 * RequestContextResolver resolver = ...
 *
 * @Get
 * @InstrumentServerContext
 * void helloWorld(long id) {
 *  Context = resolver.currentRequest()
 *   ...
 * }
 *
 * }</pre>
 */
@Retention(SOURCE)
@Target({TYPE, METHOD})
public @interface InstrumentServerContext {}
