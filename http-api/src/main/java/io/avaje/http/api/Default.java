package io.avaje.http.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Define a default value for a form parameter or query parameter.
 *
 * <h4>Example</h4>
 *
 * <pre>{@code
 *
 * @Get("/forCustomer/{custId}")
 * public List<Contact> getContacts(UUID custId, @Default("name") String orderBy) {
 *   ...
 * }
 *
 * }</pre>
 */
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
public @interface Default {

  /** The default values. */
  String[] value();
}
