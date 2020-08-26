package org.example.myapp;

import io.avaje.http.client.HttpClientContext;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ReqScopedControllerTest extends BaseWebTest {

  private final HttpClientContext client = client();

  @Test
  void testGet() {

    final HttpResponse<String> res = client.request()
      .path("req-scoped")
      .get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("http://localhost:8887/req-scoped");
  }
}
