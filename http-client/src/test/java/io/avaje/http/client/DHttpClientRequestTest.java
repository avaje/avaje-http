package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class DHttpClientRequestTest {

  final DHttpClientContext context = new DHttpClientContext(null, null, null, null, null, null, null, null);

  @Test
  void suppressLogging_listenerEvent_expect_suppressedPayloadContent() {
    final DHttpClientRequest request = new DHttpClientRequest(context, Duration.ZERO);

    request.suppressLogging();
    final RequestListener.Event event = request.listenerEvent();

    assertThat(event.requestBody()).isEqualTo("<suppressed request body>");
    assertThat(event.responseBody()).isEqualTo("<suppressed response body>");
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
