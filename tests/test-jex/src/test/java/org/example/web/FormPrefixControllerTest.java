package org.example.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

class FormPrefixControllerTest extends BaseWebTest {

  @Test
  void nestedFormPrefix() {
    HttpResponse<String> res = client().request()
      .path("formprefix")
      .formParam("name", "bill")
      .formParam("invoice.street", "Main Street")
      .formParam("invoice.city", "Springfield")
      .formParam("invoice.zip", "12345")
      .formParam("invoice.contact.phone", "555-1234")
      .formParam("invoice.contact.email", "bob@example.com")
      .formParam("shipping.street", "Square One")
      .formParam("shipping.city", "Shelbyville")
      .POST()
      .asString();

    assertThat(res.statusCode()).isEqualTo(201);
    assertThat(res.body()).isEqualTo(
      "bill|Main Street,Springfield,12345|555-1234@bob@example.com|Square One,Shelbyville");
  }

  @Test
  void nestedFormPrefix_partial() {
    HttpResponse<String> res = client().request()
      .path("formprefix")
      .formParam("name", "ann")
      .POST()
      .asString();

    assertThat(res.statusCode()).isEqualTo(201);
    assertThat(res.body()).isEqualTo("ann|null,null,null|null@null|null,null");
  }
}
