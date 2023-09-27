package io.avaje.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class HelloBasicAuthTest extends BaseWebTest {

  final HttpClient client = client();

  public static HttpClient client() {
    return HttpClient.builder()
      .baseUrl(baseUrl)
      .bodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
      .requestIntercept(new BasicAuthIntercept("rob", "bot"))
      .build();
  }

  @Test
  void basicAuth() {

    final HttpResponse<String> hres = client.request()
      .path("hello/basicAuth")
      .GET()
      .asString();

    assertThat(hres.statusCode()).isEqualTo(200);
    assertThat(hres.body()).isEqualTo("decoded: rob:bot");

    HttpClient client2 = client.toBuilder().build();

    final HttpResponse<String> hres2 = client2.request()
      .path("hello/basicAuth")
      .GET()
      .asString();

    assertThat(hres2.statusCode()).isEqualTo(200);
    assertThat(hres2.body()).isEqualTo("decoded: rob:bot");
  }

}
