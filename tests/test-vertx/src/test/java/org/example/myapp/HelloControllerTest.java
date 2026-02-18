package org.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class HelloControllerTest extends BaseWebTest {

  @Test
  void rootHello() throws Exception {
    final var response = get("/");
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("Hello World");
  }

  @Test
  void generatedHello() throws Exception {
    final var response = get("/hello");
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("hello world");
  }

  @Test
  void pathQueryAndHeaderParams() throws Exception {
    final var request = HttpRequest.newBuilder(URI.create(baseUrl + "/hello/with-params/42?q=foo"))
      .header("X-Trace", "abc-123")
      .GET()
      .build();
    final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("id=42;q=foo;trace=abc-123");
  }
}
