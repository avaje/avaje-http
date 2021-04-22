package io.avaje.http.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrlBuilderTest {

  @Test
  void url() {
    String url = new UrlBuilder("https://foo").url("http://bar").path("bazz").build();
    assertThat(url).isEqualTo("http://bar/bazz");
  }

  @Test
  void path() {
    assertThat(new UrlBuilder("https://foo").path("bar").build()).isEqualTo("https://foo/bar");
  }

  @Test
  void matrixParam() {
    final String url = new UrlBuilder("https://foo").path("bar").matrixParam("a", "one").matrixParam("b", "two")
      .build();

    assertThat(url).isEqualTo("https://foo/bar;a=one;b=two");
  }

  @Test
  void matrixParam_null() {
    final String url = new UrlBuilder("https://foo").path("bar").matrixParam("a", null).matrixParam("b", "two")
      .build();

    assertThat(url).isEqualTo("https://foo/bar;b=two");
  }

  @Test
  void matrixParam_path() {
    final String url = new UrlBuilder("https://foo")
      .path("bar").matrixParam("a", null).matrixParam("b", "two")
      .path("foo")
      .build();

    assertThat(url).isEqualTo("https://foo/bar;b=two/foo");
  }

  @Test
  void param() {
    assertThat(new UrlBuilder("https://foo").queryParam("bar", null).build()).isEqualTo("https://foo");
    assertThat(new UrlBuilder("https://foo").queryParam("bar", "a").build()).isEqualTo("https://foo?bar=a");
    assertThat(new UrlBuilder("https://foo").queryParam("bar", "a").queryParam("baz", "b").build()).isEqualTo("https://foo?bar=a&baz=b");
  }

  @Test
  void param_encode() {
    assertThat(new UrlBuilder("https://foo").queryParam("some one", "a%b").build()).isEqualTo("https://foo?some+one=a%25b");
  }

  @Test
  void env() {
    assertThat(UrlBuilder.enc("some one")).isEqualTo("some+one");
    assertThat(UrlBuilder.enc("a%b")).isEqualTo("a%25b");
  }
}
