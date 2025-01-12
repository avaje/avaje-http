package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BearerTokenInterceptTest {

  @Test
  void beforeRequest() {
    // setup
    final var intercept = new BearerTokenIntercept("api_key");
    final var ctx = HttpClient.builder().baseUrl("junk").build();

    // act
    final HttpClientRequest request = ctx.request();
    intercept.beforeRequest(request);

    final List<String> values = request.header("Authorization");
    assertThat(values).containsExactly("Bearer api_key");
  }

}
