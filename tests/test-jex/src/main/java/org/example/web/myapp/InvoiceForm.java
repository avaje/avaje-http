package org.example.web.myapp;

import io.avaje.http.api.FormParam;

public record InvoiceForm(
    @FormParam("street") String street,
    @FormParam("city") String city) {}
