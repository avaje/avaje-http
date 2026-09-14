package org.example;

import io.avaje.http.api.FormParam;

public record InvoiceForm(
    @FormParam("street") String street,
    @FormParam("city") String city) {}
