package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DUrlBuilderTest {

  @Test
  void clone_expect_copyOfOriginal() {
    var uriBuilder = UrlBuilder.of("http://foo")
      .path("more");

    UrlBuilder copy = uriBuilder.clone();
    copy.queryParam("a", "a");

    uriBuilder.queryParam("b", "b");
    assertThat(uriBuilder.build()).isEqualTo("http://foo/more?b=b");
    assertThat(copy.build()).isEqualTo("http://foo/more?a=a");
  }

  @Test
  void clone_hasParams_expect_copyOfOriginal() {
    var uriBuilder = UrlBuilder.of("http://foo").path("more").queryParam("orig",1);

    UrlBuilder copy = uriBuilder.clone();
    copy.queryParam("a", "a");

    uriBuilder.queryParam("b", "b");
    assertThat(uriBuilder.build()).isEqualTo("http://foo/more?orig=1&b=b");
    assertThat(copy.build()).isEqualTo("http://foo/more?orig=1&a=a");
  }

  @Test
  void url() {
    var uri = UrlBuilder.of("http://foo")
      .path("more")
      .build();

    assertThat(uri).isEqualTo("http://foo/more");
  }

  @Test
  void url_when_httpPrefix() {
    var uri = UrlBuilder.of("http://foo")
      .url("http://bar")
      .path("more")
      .build();

    assertThat(uri).isEqualTo("http://bar/more");
  }

  @Test
  void path_basic() {
    var uri = UrlBuilder.of("http://foo")
      .path("more")
      .build();

    assertThat(uri).isEqualTo("http://foo/more");
  }

  @Test
  void path_when_httpPrefix() {
    var uri = UrlBuilder.of("http://foo")
      .path("http://bar")
      .path("more")
      .build();

    assertThat(uri).isEqualTo("http://bar/more");
  }
}
