package io.avaje.http.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A parameter that is a bean containing query parameters, headers and cookies.
 * <p>
 * The properties on the bean are by default treated as query parameters.
 * </p>
 * <p>
 * This is functionally very similar to <code>@Form</code> but intended for
 * use with all other cases than FORM POST.
 * </p>
 *
 * <h4>Example</h4>
 * <p>
 * Simple bean parameter, properties default to query parameters matching the property name.
 * </p>
 *
 * <pre>{@code
 *
 *   public class MyParams {
 *
 *     public String filter;
 *
 *     @Default("name")
 *     public String orderBy;
 *
 *     public int firstRow;
 *     public int maxRows
 *
 *     @Header
 *     public String lastModified;
 *   }
 *
 *   ...
 *
 *   @Get("/search")
 *   List<Customer> findCustomers(@BeanParam MyParams params) {
 *     ...
 *   }
 *
 * }</pre>
 */
@Target(value={PARAMETER,METHOD})
@Retention(value=RUNTIME)
public @interface BeanParam {

}
