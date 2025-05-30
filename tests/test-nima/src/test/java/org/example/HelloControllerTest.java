package org.example;

import org.junit.jupiter.api.Test;
import java.net.http.HttpResponse;


import static org.assertj.core.api.Assertions.assertThat;

class HelloControllerTest extends BaseWebTest {

  @Test
  void listParamOne() {
    HttpResponse<String> res = client().request()
      .path("listParams")
      .queryParam("codes", "123")
      .GET()
      .asPlainString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("codes:[123]");
  }

  @Test
  void listParamMultiple() {
    HttpResponse<String> res = client().request()
      .path("listParams")
      .queryParam("codes", "123")
      .queryParam("codes", "456")
      .GET()
      .asPlainString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("codes:[123, 456]");
  }

  @Test
  void listParamEmpty() {
    HttpResponse<String> res = client().request()
      .path("listParams")
      .GET()
      .asPlainString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("codes:[]");
  }
}
