package io.avaje.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestListenerTest extends BaseWebTest {

  static class TDRequestListener implements RequestListener {
    final boolean hasBody;

    TDRequestListener(boolean hasBody) {
      this.hasBody = hasBody;
    }

    @Override
    public void response(Event event) {
      if (hasBody) {
        assertThat(event.responseBody()).isEqualTo("post");
        assertThat(event.uri().toString()).isEqualTo("http://localhost:8887/post");
        assertThat(event.requestBody()).isEqualTo("post-request-body");
      } else {
        assertThat(event.responseBody()).isEqualTo("hello world");
        assertThat(event.uri().toString()).isEqualTo("http://localhost:8887/hello/message");
        assertThat(event.requestBody()).isNull();
      }
      assertThat(event.responseTimeMicros()).isGreaterThan(1L);
      assertThat(event.response().statusCode()).isEqualTo(200);
      assertThat(event.request()).isEqualTo(event.response().request());
    }
  }

  private HttpClientContext createClient(TDRequestListener tdRequestListener) {
    return HttpClientContext.newBuilder()
      .withBaseUrl(baseUrl)
      .withRequestListener(new RequestLogger())
      .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
      .withRequestListener(tdRequestListener)
      .build();
  }

  @Test
  void get_no_request_body() {
    final TDRequestListener tdRequestListener = new TDRequestListener(false);
    final HttpClientContext client = createClient(tdRequestListener);

    final HttpResponse<String> hres = client.request()
      .path("hello").path("message")
      .GET().asString();

    assertThat(hres.body()).contains("hello world");
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void post() {
    final TDRequestListener tdRequestListener = new TDRequestListener(true);
    final HttpClientContext client = createClient(tdRequestListener);

    final HttpResponse<String> hres = client.request()
      .path("post")
      .body("post-request-body")
      .POST().asString();

    assertThat(hres.body()).contains("post");
    assertThat(hres.statusCode()).isEqualTo(200);
  }

}
