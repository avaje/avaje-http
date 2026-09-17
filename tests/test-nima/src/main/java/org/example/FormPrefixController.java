package org.example;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Form;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;

@Controller
public class FormPrefixController {

    @Produces("text/plain")
    @Post("formprefix")
    String submit(@Form PersonForm form) {
        return form.name
                + "|" + form.birthDate
                + "|" + form.hobbies
                + "|" + form.invoice.street + "," + form.invoice.city + "," + form.invoice.getPostalCode()
                + "|" + form.invoice.contact.phone + "@" + form.invoice.contact.email
                + "|" + form.shipping.street + "," + form.shipping.city;
    }
}
