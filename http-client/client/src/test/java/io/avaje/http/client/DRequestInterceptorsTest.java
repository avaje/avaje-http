package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DRequestInterceptorsTest {

  private final StringBuilder buffer = new StringBuilder();

  @Test
  void intercept_reverse_after() {

    DRequestInterceptors interceptors = new DRequestInterceptors(asList(new One(), new Two()));

    interceptors.beforeRequest(mock(HttpClientRequest.class));
    interceptors.afterResponse(mock(HttpResponse.class), mock(HttpClientRequest.class));

    assertThat(buffer.toString()).isEqualTo("oneBefore|twoBefore|twoAfter|oneAfter|");
  }

  private class One implements RequestIntercept {

    @Override
    public void beforeRequest(HttpClientRequest request) {
      buffer.append("oneBefore|");
    }

    @Override
    public void afterResponse(HttpResponse<?> response, HttpClientRequest request) {
      buffer.append("oneAfter|");
    }
  }

  private class Two implements RequestIntercept {

    @Override
    public void beforeRequest(HttpClientRequest request) {
      buffer.append("twoBefore|");
    }

    @Override
    public void afterResponse(HttpResponse<?> response, HttpClientRequest request) {
      buffer.append("twoAfter|");
    }
  }
}
