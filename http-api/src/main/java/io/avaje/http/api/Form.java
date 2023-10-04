package io.avaje.http.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A parameter that is a bean containing form parameters.
 * <p>
 * The properties on the bean are by default treated as form parameters.
 * If they have no other annotations like <code>@QueryParam</code>, or
 * <code>@Header</code> etc they are populated as form parameters.
 * </p>
 * <p>
 * The properties can have other annotations like <code>@QueryParam</code>,
 * <code>@Header</code>, <code>@Cookie</code>, <code>@Default</code>.
 * </p>
 * <p>
 * We would explicitly annotate a property with <code>@FormParam</code> if
 * the form property name is snake case or similar that doesn't map to a
 * valid java/kotlin variable.
 * </p>
 *
 * <h4>Example 1</h4>
 * <p>
 * Simple form bean, properties default to form parameters matching the property name.
 * </p>
 *
 * <pre>{@code
 *   public class MyForm {
 *
 *     public String id;
 *     public String name;
 *   }
 *
 *   ...
 *
 *   @Post
 *   void postForm(@Form MyForm fooForm) {
 *
 *     ...
 *   }
 *
 * }</pre>
 *
 * <h4>Example 2</h4>
 * <p>
 * Form bean with various annotations.
 * </p>
 *
 * <pre>{@code
 *   public class MyForm {
 *
 *     @FormParam("start-date")
 *     public LocalDate startDate;
 *
 *     @Default("Fred")
 *     public String myName;
 *
 *     @Cookie
 *     public String lastActive;
 *   }
 *
 * }</pre>
 */
@Target({PARAMETER,METHOD})
@Retention(RUNTIME)
public @interface Form {

}
