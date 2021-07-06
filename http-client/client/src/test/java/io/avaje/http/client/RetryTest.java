package io.avaje.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class RetryTest extends BaseWebTest {

  final MyIntercept myIntercept = new MyIntercept();
  final HttpClientContext clientContext = initClientWithRetry();

  HttpClientContext initClientWithRetry() {
    return HttpClientContext.newBuilder()
      .withBaseUrl("http://localhost:8887")
      .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
      .withRequestListener(new RequestLogger())
      .withRetryHandler(new SimpleRetryHandler(4, 1))
      .withRequestIntercept(myIntercept)
      .build();
  }

  @Test
  void retryTest() {
    HttpResponse<String> res = clientContext.request()
      .label("http_client_hello_retry")
      .path("hello/retry")
      .GET()
      .asString();

    assertThat(res.body()).isEqualTo("All good at 3rd attempt");

    assertThat(myIntercept.responseTimeMicros).isGreaterThan(1);
    assertThat(myIntercept.counter).isEqualTo(1);
    assertThat(myIntercept.label).isEqualTo("http_client_hello_retry");
  }

  static class MyIntercept implements RequestIntercept {
    int counter;
    long responseTimeMicros;
    String label;

    /**
     * Not called for the retry attempts. Only called on the final success or error response.
     */
    @Override
    public void afterResponse(HttpResponse<?> response, HttpClientRequest request) {
      counter++;
      responseTimeMicros = request.responseTimeMicros();
      label = request.label();
    }
  }
}
