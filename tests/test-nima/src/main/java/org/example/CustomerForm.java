package org.example;

import java.util.List;

import io.avaje.http.api.FormParam;
import io.avaje.http.api.FormPrefix;

public record CustomerForm(
    @FormParam("firstName") String firstName,
    List<String> lastName,
    @FormPrefix("invoice") InvoiceForm invoice) {}
