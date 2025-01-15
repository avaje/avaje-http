package io.avaje.http.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DHttpClientRequestTest {

  final DHttpClientContext context = new DHttpClientContext(null, null, null, null, null, null, null, null, null);

  @Test
  void suppressLogging_listenerEvent_expect_suppressedPayloadContent() {
    final DHttpClientRequest request = new DHttpClientRequest(context, Duration.ZERO);

    request.suppressLogging();
    final RequestListener.Event event = request.listenerEvent();

    assertThat(event.requestBody()).isEqualTo("<suppressed request body>");
    assertThat(event.responseBody()).isEqualTo("<suppressed response body>");
  }

  @Test
  void assertHeader() {
    final var request = new DHttpClientRequest(context, Duration.ZERO);

    final var headers =
        request
            .header("Accept", (Object) List.of("application/json", "application/json2"))
            .header("Accept");

    assertThat(headers).asList().contains("application/json", "application/json2");
  }

  @Disabled
  @Test
  @Disabled
  void assertQuery() {
    final var client = HttpClient.builder().baseUrl("https://ap7i.github.com").build();

    final var uri =
        client
            .request()
            .queryParam("param", List.of("param1", "param2"))
            .HEAD()
            .asDiscarding()
            .request()
            .uri()
            .toString();

    assertThat(uri).isEqualTo("https://ap7i.github.com?param=param1&param=param2");
  }

  @Test
  void skipAuthToken_listenerEvent_expect_suppressedPayloadContent() {
    final DHttpClientRequest request = new DHttpClientRequest(context, Duration.ZERO);
    assertThat(request.isSkipAuthToken()).isFalse();

    request.skipAuthToken();
    assertThat(request.isSkipAuthToken()).isTrue();

    final RequestListener.Event event = request.listenerEvent();

    assertThat(event.requestBody()).isEqualTo("<suppressed request body>");
    assertThat(event.responseBody()).isEqualTo("<suppressed response body>");
  }
}
