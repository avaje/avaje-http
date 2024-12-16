package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BasicAuthInterceptTest {

  @Test
  void encode() {
    final String encode = BasicAuthIntercept.encode("Aladdin", "open sesame");
    assertThat(encode).isEqualTo("QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
  }

  @Test
  void header() {
    final String encode = BasicAuthIntercept.header("Aladdin", "open sesame");
    assertThat(encode).isEqualTo("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
  }

  @Test
  void beforeRequest() {
    // setup
    final BasicAuthIntercept intercept = new BasicAuthIntercept("Aladdin", "open sesame");
    final var ctx = HttpClient.builder().baseUrl("junk").build();

    // act
    final HttpClientRequest request = ctx.request();
    intercept.beforeRequest(request);

    final List<String> values = request.header("Authorization");
    assertThat(values).containsExactly("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
  }
}
