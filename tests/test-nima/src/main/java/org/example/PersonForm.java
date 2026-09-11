package org.example;

import io.avaje.http.api.FormParam;
import io.avaje.http.api.FormPrefix;
import java.util.List;

/**
 * Complex form bean exercising {@link FormPrefix} nested binding:
 * flat fields, type conversion, a collection, two nested beans under
 * different prefixes and deep (two level) nesting.
 */
public class PersonForm {

    @FormParam("name")
    public String name;

    @FormParam("birth-date")
    public java.time.LocalDate birthDate;

    public List<String> hobbies;

    @FormPrefix("invoice")
    public AddressForm invoice;

    @FormPrefix("shipping")
    public AddressForm shipping;
}
