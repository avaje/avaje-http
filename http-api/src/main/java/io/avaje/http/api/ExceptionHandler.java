package io.avaje.http.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for handling exceptions in controller classes and/or handler methods.
 *
 * <p>Handler methods which are annotated with this annotation are allowed to have very flexible
 * signatures. They may have parameters of the following types:
 *
 * <ol>
 *   <li>An exception argument: declared as a general Exception or as a more specific exception.
 *       This also serves as a mapping hint if the annotation itself does not narrow the exception
 *       types through its {@link #value()}.
 *   <li>Request and/or response objects (typically from the microframework). You may choose any
 *       specific request/response type, e.g. Javalin's {@code io.javalin.Context} or Helidon's
 *       ServerRequest/ServerResponse.
 * </ol>
 *
 * <p>Handler methods may be void or return an object for serialization.
 *
 * <p>You may combine the {@code ExceptionHandler} annotation with {@link Produces @Produces} for a
 * specific HTTP error status and media type.
 */
@Documented
@Target(METHOD)
@Retention(SOURCE)
public @interface ExceptionHandler {

  /**
   * Exception handled by the annotated method. If empty, will default to any exception listed in
   * the method argument list.
   */
  Class<? extends Exception> value() default DefaultException.class;
}
