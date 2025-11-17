package io.avaje.http.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DHttpClientRequestTest {

  final DHttpClientContext context = new DHttpClientContext(null, null, null, null, null, null, null, null, null, null);

  @Test
  void suppressLogging_listenerEvent_expect_suppressedPayloadContent() {
    final DHttpClientRequest request = new DHttpClientRequest(context, Duration.ZERO);

    request.suppressLogging();
    final RequestListener.Event event = request.listenerEvent();

    assertThat(event.requestBody()).isEqualTo("<suppressed request body>");
    assertThat(event.responseBody()).isEqualTo("<suppressed response body>");
  }

  @Test
  void clone_expect_deepCopiesOfOriginalRequest() {
    final DHttpClientRequest request = new DHttpClientRequest(context, Duration.ZERO);
    request.suppressLogging();
    request.path("patha");
    request.queryParam("queryParam", "b");
    request.header("aheader", "orig");
    request.body("SomeBody");
    request.setAttribute("foo", "one");
    request.formParam("fp", "1");
    request.formParam("fp", "2");

    HttpClientRequest req0 = request.clone();
    HttpClientRequest req1 = request.clone();

    request.queryParam("orig", "x");
    req0.queryParam("req0", "y");
    req1.queryParam("req1", "z");

    request.header("aheader", "x");
    req0.header("aheader", "y");
    req0.formParam("fp2", "5");

    req1.header("aheader", "z");
    req1.header("req1", "2");
    req1.setAttribute("bar", "two");
    req1.formParam("fp", "3");

    assertThat(request.url()).isEqualTo("null/patha?queryParam=b&orig=x");
    assertThat(req0.url()).isEqualTo("null/patha?queryParam=b&req0=y");
    assertThat(req1.url()).isEqualTo("null/patha?queryParam=b&req1=z");

    assertThat(request.headers()).containsOnlyKeys("aheader");
    assertThat(request.header("aheader")).containsOnly("orig", "x");

    assertThat(req0.headers()).containsOnlyKeys("aheader");
    assertThat(req0.header("aheader")).containsOnly("orig", "y");

    assertThat(req1.headers()).containsOnlyKeys("aheader", "req1");
    assertThat(req1.header("aheader")).containsOnly("orig", "z");

    assertThat((String)req0.getAttribute("foo")).isEqualTo("one");
    assertThat((String)req0.getAttribute("bar")).isNull();

    assertThat((String)req1.getAttribute("foo")).isEqualTo("one");
    assertThat((String)req1.getAttribute("bar")).isEqualTo("two");
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
