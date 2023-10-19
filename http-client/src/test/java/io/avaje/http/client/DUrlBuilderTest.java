package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DUrlBuilderTest {

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
