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

    new InterceptorChain(
            asList(new One(), new Two()),
            () -> {
              buffer.append("call|");
              return mock(HttpResponse.class);
            })
        .proceed(mock(HttpClientRequest.class));

    assertThat(buffer.toString()).isEqualTo("oneBefore|twoBefore|call|twoAfter|oneAfter|");
  }

  @Test
  void intercept_abort() {

    new InterceptorChain(
            asList(new One(), new Skip(), new Two()),
            () -> {
              buffer.append("call|");
              return mock(HttpResponse.class);
            })
        .proceed(mock(HttpClientRequest.class));

    assertThat(buffer.toString()).isEqualTo("oneBefore|skip|oneAfter|");
  }

  @Test
  void intercept_noProceed() {

    new InterceptorChain(
            asList(new One(), new PassThrough(), new Two()),
            () -> {
              buffer.append("call|");
              return mock(HttpResponse.class);
            })
        .proceed(mock(HttpClientRequest.class));

    assertThat(buffer.toString()).isEqualTo("oneBefore|pass|call|oneAfter|");
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

  private class Skip implements RequestIntercept {

    @Override
    public void intercept(HttpClientRequest request, InterceptChain chain) {
      buffer.append("skip|");
      chain.setResponse(mock(HttpResponse.class));
    }
  }

  private class PassThrough implements RequestIntercept {

    @Override
    public void intercept(HttpClientRequest request, InterceptChain chain) {
      buffer.append("pass|");
    }
  }
}
