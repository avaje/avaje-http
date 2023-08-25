package io.avaje.http.client;

import org.example.github.BasicClientInterface;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DHttpClientContextTest {

  private final DHttpClientContext context = new DHttpClientContext(null, null, null, null, null, null, null, null, null);

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

    final HttpClient client =
      HttpClient.builder()
      .baseUrl("http://localhost")
      .build();

    SpiHttpClient spi = (SpiHttpClient)client;
    // has default client created
    assertThat(spi.httpClient()).isNotNull();
    assertThat(spi.httpClient().version()).isEqualTo(java.net.http.HttpClient.Version.HTTP_2);
    assertThat(spi.httpClient().cookieHandler()).isPresent();

    // has expected url building
    assertThat(client.url().build()).isEqualTo("http://localhost");
    assertThat(client.url().path("hello").build()).isEqualTo("http://localhost/hello");
    assertThat(client.url().queryParam("hello","there").build()).isEqualTo("http://localhost?hello=there");
  }

  @Test
  void build_noCookieHandler() {

    final HttpClient client =
      HttpClient.builder()
        .baseUrl("http://localhost")
        .cookieHandler(null)
        .redirect(java.net.http.HttpClient.Redirect.ALWAYS)
        .build();

    SpiHttpClient spi = (SpiHttpClient)client;
    // has default client created
    assertThat(spi.httpClient()).isNotNull();
    assertThat(spi.httpClient().version()).isEqualTo(java.net.http.HttpClient.Version.HTTP_2);
    assertThat(spi.httpClient().cookieHandler()).isEmpty();

    // has expected url building
    assertThat(client.url().build()).isEqualTo("http://localhost");
    assertThat(client.url().path("hello").build()).isEqualTo("http://localhost/hello");
    assertThat(client.url().queryParam("hello","there").build()).isEqualTo("http://localhost?hello=there");
  }

  @Test
  void build_missingBaseUrl() {
    try {
      HttpClient.builder().build();
    } catch (NullPointerException e) {
      assertThat(e.getMessage()).contains("baseUrl is not specified");
    }
  }

}
