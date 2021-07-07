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
      .baseUrl("http://localhost:8887")
      .bodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
      .requestListener(new RequestLogger())
      .retryHandler(new SimpleRetryHandler(4, 1))
      .requestIntercept(myIntercept)
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
    assertThat(myIntercept.customAttributeTimeMillis).isGreaterThan(1);

    assertThat(myIntercept.counter).isEqualTo(1);
    assertThat(myIntercept.label).isEqualTo("http_client_hello_retry");
  }

  static class MyIntercept implements RequestIntercept {
    int counter;
    long responseTimeMicros;
    long customAttributeTimeMillis;
    String label;

    @Override
    public void beforeRequest(HttpClientRequest request) {
      final String label = request.setAttribute("MY_START_TIME", System.currentTimeMillis()).label();
      assertThat(label).isEqualTo("http_client_hello_retry");
    }

    /**
     * Not called for the retry attempts. Only called on the final success or error response.
     */
    @Override
    public void afterResponse(HttpResponse<?> response, HttpClientRequest request) {

      final String does_not_exist = request.getAttribute("DOES_NOT_EXIST");
      assertThat(does_not_exist).isNull();

      long start = request.getAttribute("MY_START_TIME");
      customAttributeTimeMillis = System.currentTimeMillis() - start;
      counter++;
      responseTimeMicros = request.responseTimeMicros();
      label = request.label();
    }
  }
}
