package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specify endpoint response status code/description/type.
 *
 * <p>When not specified the default 2xx openAPI generation is based on the javadoc of the method.
 *
 * <p>Will not override the default 2xx generated openapi unless status code is 2xx
 *
 * <pre>{@code
 * @Post("/post")
 * @OpenAPIReturns(responseCode = "200", description = "from annotaion")
 * @OpenAPIReturns(responseCode = "201")
 * @OpenAPIReturns(responseCode = "500", description = "Some other Error", type=ErrorResponse.class)
 * ResponseModel endpoint() {}
 *
 * }</pre>
 *
 * <p>Can also be placed on a class to add to every method in the controller.
 *
 * <pre>{@code
 * @OpenAPIResponse(
 * responseCode = "403",
 * description = "Insufficient rights to this resource."
 * )
 * public class MyController {
 * ...
 * }
 * }</pre>
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Repeatable(OpenAPIResponses.class)
public @interface OpenAPIResponse {

  /** the http status code of this response */
  int responseCode();

  /**
   * The description of the return value. By default uses the @return javadoc of the method as the
   * description
   */
  String description() default "";

  /**
   * The concrete type that that this endpoint returns. If status code is a 2xx code it will default
   * to the return type of the method
   */
  Class<?> type() default Void.class;
}
