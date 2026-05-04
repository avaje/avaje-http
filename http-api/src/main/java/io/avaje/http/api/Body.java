package io.avaje.http.api;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a controller method parameter to be read from the request body.
 * <p>
 * Use this to explicitly force body binding when a parameter could otherwise be interpreted as a
 * query parameter (or form parameter when the method is marked {@link Form}). This is commonly
 * used for scalar values and scalar collections such as {@code String} and {@code List<Long>}.
 * </p>
 *
 * <h4>Example</h4>
 *
 * <pre>{@code
 * @Controller
 * class ProductController {
 *
 *   @Post("/products/filter")
 *   String filter(@Body List<String> codes) {
 *     return "codes:" + codes;
 *   }
 * }
 * }</pre>
 */
@Retention(SOURCE)
@Target(PARAMETER)
public @interface Body {}
