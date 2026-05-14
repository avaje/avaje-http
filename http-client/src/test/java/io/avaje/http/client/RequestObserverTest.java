package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RequestObserverTest extends BaseWebTest {

  @Test
  void syncRequest_observesSingleAttempt() {
    final RecordingObserver observer = new RecordingObserver();
    final HttpClient client = client(observer, null);

    final HttpResponse<String> response = client.request()
      .path("hello")
      .path("message")
      .GET()
      .asString();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(observer.startCount.get()).isEqualTo(1);
    assertThat(observer.resendCounts).containsExactly(0);
    assertThat(observer.statusCodes).containsExactly(200);
    assertThat(observer.errors).isEmpty();
  }

  @Test
  void asyncRequest_observesSingleAttempt() {
    final RecordingObserver observer = new RecordingObserver();
    final HttpClient client = client(observer, null);

    final HttpResponse<String> response = client.request()
      .path("hello")
      .path("message")
      .GET()
      .async()
      .asString()
      .join();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(observer.startCount.get()).isEqualTo(1);
    assertThat(observer.resendCounts).containsExactly(0);
    assertThat(observer.statusCodes).containsExactly(200);
    assertThat(observer.errors).isEmpty();
  }

  @Test
  void retrySync_observesEachAttempt() {
    final RecordingObserver observer = new RecordingObserver();
    final HttpClient client = client(observer, new SimpleRetryHandler(4, 1));

    final HttpResponse<String> response = client.request()
      .path("hello")
      .path("retry")
      .GET()
      .asString();

    assertThat(response.body()).isEqualTo("All good at 3rd attempt");
    assertThat(observer.startCount.get()).isEqualTo(1);
    assertThat(observer.resendCounts).containsExactly(0, 1, 2);
    assertThat(observer.statusCodes).containsExactly(500, 500, 200);
    assertThat(observer.errors).isEmpty();
  }

  @Test
  void retryAsync_observesEachAttempt() {
    final RecordingObserver observer = new RecordingObserver();
    final HttpClient client = client(observer, new SimpleRetryHandler(4, 1));

    final HttpResponse<String> response = client.request()
      .path("hello")
      .path("retry")
      .GET()
      .async()
      .asString()
      .join();

    assertThat(response.body()).isEqualTo("All good at 3rd attempt");
    assertThat(observer.startCount.get()).isEqualTo(1);
    assertThat(observer.resendCounts).containsExactly(0, 1, 2);
    assertThat(observer.statusCodes).containsExactly(500, 500, 200);
    assertThat(observer.errors).isEmpty();
  }

  private HttpClient client(RequestObserver observer, RetryHandler retryHandler) {
    final HttpClient.Builder builder = HttpClient.builder()
      .baseUrl(baseUrl)
      .requestLogging(false)
      .bodyAdapter(new JacksonBodyAdapter())
      .requestObserver(observer);
    if (retryHandler != null) {
      builder.retryHandler(retryHandler);
    }
    return builder.build();
  }

  static final class RecordingObserver implements RequestObserver {

    final AtomicInteger startCount = new AtomicInteger();
    final List<Integer> resendCounts = Collections.synchronizedList(new ArrayList<>());
    final List<Integer> statusCodes = Collections.synchronizedList(new ArrayList<>());
    final List<String> errors = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Observation start(HttpClientRequest request) {
      startCount.incrementAndGet();
      return (attemptRequest, resendCount) -> {
        resendCounts.add(resendCount);
        return new Attempt() {
          @Override
          public void onResponse(HttpResponse<?> response) {
            statusCodes.add(response.statusCode());
          }

          @Override
          public void onError(Throwable error) {
            errors.add(error.getClass().getName());
          }
        };
      };
    }
  }
}
