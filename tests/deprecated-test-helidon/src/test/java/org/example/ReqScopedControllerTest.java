package org.example;

import io.avaje.http.client.HttpClient;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ReqScopedControllerTest extends BaseWebTest {

  private final HttpClient client = client();

  @Test
  void testGet() {

    final HttpResponse<String> res = client.request()
      .path("req-scoped")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("/req-scoped-200 OK");
  }
}
