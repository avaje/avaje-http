package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DHttpClientRequestTest {

  @Test
  void suppressLogging_listenerEvent_expect_suppressedPayloadContent() {

    final DHttpClientRequest request = new DHttpClientRequest(mock(DHttpClientContext.class), Duration.ZERO);

    request.suppressLogging();
    final RequestListener.Event event = request.listenerEvent();

    assertThat(event.requestBody()).isEqualTo("<suppressed request body>");
    assertThat(event.responseBody()).isEqualTo("<suppressed response body>");
  }

  @Test
  void skipAuthToken_listenerEvent_expect_suppressedPayloadContent() {

    final DHttpClientRequest request = new DHttpClientRequest(mock(DHttpClientContext.class), Duration.ZERO);
    assertThat(request.isSkipAuthToken()).isFalse();

    request.skipAuthToken();
    assertThat(request.isSkipAuthToken()).isTrue();

    final RequestListener.Event event = request.listenerEvent();

    assertThat(event.requestBody()).isEqualTo("<suppressed request body>");
    assertThat(event.responseBody()).isEqualTo("<suppressed response body>");
  }
}
