package io.avaje.http.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.MODULE;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specify the path mapping request to a controller. When placed on a package-info or a module-info
 * file, all routes in the package/module will have value added as a prefix
 *
 * <pre>{@code
 * @Controller
 * @Path("/customers")
 * class CustomerController {
 *   ...
 * }
 * }</pre>
 * <pre>{@code
 * @Path("/customers") // all routes in this module will have a customers prefix
 * module example.module {
 *   ...
 * }
 *
 * }</pre>
 */
@Target({TYPE, PACKAGE, MODULE})
@Retention(RUNTIME)
public @interface Path {
  String value();
}
