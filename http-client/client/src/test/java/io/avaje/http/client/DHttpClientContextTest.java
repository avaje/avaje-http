package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DHttpClientContextTest {

  private final DHttpClientContext context = new DHttpClientContext(null, null, null, null, null, null, null);

  @Test
  void gzip_gzipDecode() {

    final byte[] asBytes = GzipUtil.gzip("HelloThere");
    final byte[] decoded = GzipUtil.gzipDecode(asBytes);
    assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo("HelloThere");

    final byte[] decoded2 = context.decodeContent("gzip", asBytes);
    assertThat(new String(decoded2, StandardCharsets.UTF_8)).isEqualTo("HelloThere");
  }

  @Test
  void gzip_contentDecode() {

    final byte[] asBytes = GzipUtil.gzip("HelloThere gzip_contentDecode");
    final byte[] decoded2 = context.decodeContent("gzip", asBytes);
    assertThat(new String(decoded2, StandardCharsets.UTF_8)).isEqualTo("HelloThere gzip_contentDecode");
  }

  @Test
  void build_basic() {

    final HttpClientContext context =
      HttpClientContext.newBuilder()
      .withBaseUrl("http://localhost")
      .build();

    // has default client created
    assertThat(context.httpClient()).isNotNull();
    assertThat(context.httpClient().version()).isEqualTo(HttpClient.Version.HTTP_2);
    assertThat(context.httpClient().cookieHandler()).isPresent();

    // has expected url building
    assertThat(context.url().build()).isEqualTo("http://localhost");
    assertThat(context.url().path("hello").build()).isEqualTo("http://localhost/hello");
    assertThat(context.url().queryParam("hello","there").build()).isEqualTo("http://localhost?hello=there");
  }

  @Test
  void build_noCookieHandler() {

    final HttpClientContext context =
      HttpClientContext.newBuilder()
        .withBaseUrl("http://localhost")
        .withCookieHandler(null)
        .withRedirect(HttpClient.Redirect.ALWAYS)
        .build();

    // has default client created
    assertThat(context.httpClient()).isNotNull();
    assertThat(context.httpClient().version()).isEqualTo(HttpClient.Version.HTTP_2);
    assertThat(context.httpClient().cookieHandler()).isEmpty();

    // has expected url building
    assertThat(context.url().build()).isEqualTo("http://localhost");
    assertThat(context.url().path("hello").build()).isEqualTo("http://localhost/hello");
    assertThat(context.url().queryParam("hello","there").build()).isEqualTo("http://localhost?hello=there");
  }

  @Test
  void build_missingBaseUrl() {
    try {
      HttpClientContext.newBuilder().build();
    } catch (NullPointerException e) {
      assertThat(e.getMessage()).contains("baseUrl is not specified");
    }
  }

}
