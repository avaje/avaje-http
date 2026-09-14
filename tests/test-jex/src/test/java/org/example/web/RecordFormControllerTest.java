package org.example.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

class RecordFormControllerTest extends BaseWebTest {

  @Test
  void recordFormPrefix() {
    HttpResponse<String> res = client().request()
      .path("recordform")
      .formParam("firstName", "bill")
      .formParam("lastName", "smith")
      .formParam("lastName", "jones")
      .formParam("invoice.street", "Main Street")
      .formParam("invoice.city", "Springfield")
      .POST()
      .asString();

    assertThat(res.statusCode()).isEqualTo(201);
    assertThat(res.body()).isEqualTo("bill|[smith, jones]|Main Street,Springfield");
  }
}
