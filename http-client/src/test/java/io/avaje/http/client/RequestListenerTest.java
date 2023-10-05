package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
        assertThat(event.uri().toString()).isEqualTo("http://localhost:8889/post");
        assertThat(event.requestBody()).isEqualTo("post-request-body");
      } else {
        assertThat(event.responseBody()).isEqualTo("hello world");
        assertThat(event.uri().toString()).isEqualTo("http://localhost:8889/hello/message");
        assertThat(event.requestBody()).isNull();
      }
      assertThat(event.responseTimeMicros()).isGreaterThan(1L);
      assertThat(event.response().statusCode()).isEqualTo(200);
      assertThat(event.request()).isEqualTo(event.response().request());
    }
  }

  private HttpClient createClient(TDRequestListener tdRequestListener) {
    return createClient(tdRequestListener, new TDReqIntercept());
  }

  private HttpClient createClient(RequestListener tdRequestListener, RequestIntercept intercept) {
    return HttpClient.builder()
      .baseUrl(baseUrl)
      .requestLogging(false)
      .requestListener(new RequestLogger())
      .bodyAdapter(new JacksonBodyAdapter())
      .requestListener(tdRequestListener)
      .requestIntercept(intercept)
      .build();
  }

  @Test
  void get_no_request_body() {
    final TDRequestListener tdRequestListener = new TDRequestListener(false);
    final HttpClient client = createClient(tdRequestListener);

    final HttpResponse<String> hres = client.request()
      .path("hello").path("message")
      .GET().asString();

    assertThat(hres.body()).contains("hello world");
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void post_bytes() {
    var tdRequestListener = new TDRequestListener(true);
    var intercept = new TDReqIntercept();
    var httpClient = createClient(tdRequestListener, intercept);

    final HttpResponse<String> hres = httpClient.request()
      .path("post")
      .body("post-request-body".getBytes(StandardCharsets.UTF_8))
      .POST().asString();

    assertThat(hres.body()).contains("post");
    assertThat(hres.statusCode()).isEqualTo(200);

    assertThat(intercept.method).isEqualTo("POST");
    assertThat(intercept.url).isEqualTo("http://localhost:8889/post");
    assertThat(intercept.bodyContent).isNotNull();

    var asString = new String(intercept.bodyContent.content(), StandardCharsets.UTF_8);
    assertThat(asString).isEqualTo("post-request-body");
  }

  @Test
  void post_string() {
    var intercept = new TDReqIntercept();
    var httpClient = createClient(event -> {}, intercept);

    final HttpResponse<String> hres = httpClient.request()
      .path("post")
      .body("post-string-body")
      .POST().asString();

    assertThat(hres.body()).contains("post");
    assertThat(hres.statusCode()).isEqualTo(200);

    assertThat(intercept.method).isEqualTo("POST");
    assertThat(intercept.url).isEqualTo("http://localhost:8889/post");
    assertThat(intercept.bodyContent).isNotNull();

    var asString = new String(intercept.bodyContent.content(), StandardCharsets.UTF_8);
    assertThat(asString).isEqualTo("post-string-body");
  }

  static class TDReqIntercept implements RequestIntercept {

    Map<String, List<String>> headers;
    String method;
    String url;
    BodyContent bodyContent;

    @Override
    public void beforeRequest(HttpClientRequest request) {
      headers = request.headers();
      method = request.method();
      url = request.url();
      bodyContent = request.bodyContent().orElse(null);
    }
  }
}
