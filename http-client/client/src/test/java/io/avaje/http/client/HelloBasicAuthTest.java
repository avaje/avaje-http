package io.avaje.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class HelloBasicAuthTest extends BaseWebTest {

  final HttpClientContext clientContext = client();

  public static HttpClientContext client() {
    return HttpClientContext.newBuilder()
      .baseUrl(baseUrl)
      .requestListener(new RequestLogger())
      .bodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
      .requestIntercept(new BasicAuthIntercept("rob", "bot"))
      .build();
  }

  @Test
  void basicAuth() {

    final HttpResponse<String> hres = clientContext.request()
      .path("hello/basicAuth")
      .GET()
      .asString();

    assertThat(hres.statusCode()).isEqualTo(200);
    assertThat(hres.body()).isEqualTo("decoded: rob:bot");
  }

}
