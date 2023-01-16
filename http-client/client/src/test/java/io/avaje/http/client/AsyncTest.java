package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncTest extends BaseWebTest {

  final HttpClientContext clientContext = client();

  @Test
  void waitForAsync()  {
    final CompletableFuture<HttpResponse<Stream<String>>> future = clientContext.request()
      .path("hello").path("stream")
      .GET()
      .async()
      .asLines();

    final AtomicBoolean flag = new AtomicBoolean();
    future.whenComplete((hres, throwable) -> {
      flag.set(true);
      assertThat(hres.statusCode()).isEqualTo(200);
      List<String> lines = hres.body().collect(Collectors.toList());
      assertThat(lines).hasSize(4);
      assertThat(lines.get(0)).contains("{\"id\":1, \"name\":\"one\"}");
    }).join();

    assertThat(flag).isTrue();
  }

}
