package io.avaje.htmx.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Mark a controller method as handling Htmx requests and potentially restrict
 * the handler to only be used for specific Htmx target or Htmx trigger.
 * <p>
 * Controller methods with {@code @HxRequest} require the {@code HX-Request}
 * HTTP Header to be set for the handler to process the request. Additionally,
 * we can specify {@link #target()}, {@link #triggerId()}, or {@link #triggerName()}
 * such that the handler is only invoked specifically for requests with those
 * matching headers.
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface HxRequest {

  /**
   * Restricts the mapping to the {@code id} of a specific target element.
   *
   * @see <a href="https://htmx.org/reference/#request_headers">HX-Target</a>
   */
  String target() default "";

  /**
   * Restricts the mapping to the {@code id} of a specific triggered element.
   *
   * @see <a href="https://htmx.org/reference/#request_headers">HX-Trigger</a>
   */
  String triggerId() default "";

  /**
   * Restricts the mapping to the {@code name} of a specific triggered element.
   *
   * @see <a href="https://htmx.org/reference/#request_headers">HX-Trigger-Name</a>
   */
  String triggerName() default "";

  /**
   * Restricts the mapping to the {@code id}, if any, or to the {@code name} of a specific triggered element.
   * <p>
   * If you want to be explicit use {@link #triggerId()} or {@link #triggerName()}.
   *
   * @see <a href="https://htmx.org/reference/#request_headers">HX-Trigger</a>
   * @see <a href="https://htmx.org/reference/#request_headers">HX-Trigger-Name</a>
   */
  String value() default "";
}
