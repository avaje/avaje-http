package io.avaje.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class RetryTest extends BaseWebTest {

  final HttpClientContext clientContext = initClientWithRetry();

  static HttpClientContext initClientWithRetry() {
    return HttpClientContext.newBuilder()
      .withBaseUrl("http://localhost:8887")
      .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
      .withRequestListener(new RequestLogger())
      .withRetryHandler(new SimpleRetryHandler(4, 1))
      .build();
  }

  @Test
  void retryTest() {

    HttpResponse<String> res = clientContext.request()
      .path("hello/retry")
      .GET()
      .asString();

    assertThat(res.body()).isEqualTo("All good at 3rd attempt");
  }

}
