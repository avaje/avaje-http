package io.avaje.http.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a field on a {@link Form} bean as a nested form object that is populated
 * from prefixed form parameters.
 * <p>
 * For example, given <code>@FormPrefix("invoice") Address invoice;</code> on a form
 * bean, the <code>Address</code> instance is populated from the form parameters
 * <code>invoice.street</code>, <code>invoice.city</code> and so on.
 * </p>
 *
 * <h4>Example</h4>
 * <pre>{@code
 *   public class Person {
 *
 *     @FormParam("name")
 *     public String name;
 *
 *     @FormPrefix("invoice")
 *     public Address invoice;
 *
 *     @FormPrefix("shipping")
 *     public Address shipping;
 *   }
 *
 *   public class Address {
 *
 *     @FormParam("street")
 *     public String street;
 *
 *     @FormParam("city")
 *     public String city;
 *   }
 *
 *   // form params: name=bill&invoice.street=xxx&invoice.city=yyy&shipping.street=zzz
 * }</pre>
 * <p>
 * The nested bean must have a no-arg constructor. Prefix mappings compose, so a
 * nested bean can itself contain <code>@FormPrefix</code> fields.
 * </p>
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface FormPrefix {

    /**
     * The prefix used to lookup the nested form parameters.
     */
    String value();
}
