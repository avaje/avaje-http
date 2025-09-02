package io.avaje.http.api;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * For this client request suppress payload logging.
 *
 * <p>Used when the payload contains sensitive content and the request and response content should
 * be suppressed.
 *
 * <pre>{@code
 * @Client
 * interface CustomerApi {
 *   ...
 *   @Get("/{id}")
 *   @SuppressLogging
 *   Customer getById(long id);
 *
 *   @Post
 *   @SuppressLogging
 *   long save(Customer customer);
 * }
 *
 * }</pre>
 */
@Retention(SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SuppressLogging {}
