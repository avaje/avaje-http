package io.avaje.http.client;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UrlBuilderTest {

  @Test
  void url() {
    String url = foo().url("http://bar").path("bazz").build();
    assertThat(url).isEqualTo("http://bar/bazz");
  }

  @Test
  void path() {
    assertThat(foo().path("bar").build()).isEqualTo("https://foo/bar");
  }

  @Test
  void path_otherTypes() {
    LocalDate date = LocalDate.of(2020, 5, 12);
    UUID uuid = UUID.randomUUID();
    assertThat(foo()
      .path(uuid)
      .path(42)
      .path(date)
      .path(98L).build()).isEqualTo("https://foo/" + uuid + "/42/2020-05-12/98");
  }

  @Test
  void matrixParam() {
    final String url = foo().path("bar").matrixParam("a", "one").matrixParam("b", "two")
      .build();

    assertThat(url).isEqualTo("https://foo/bar;a=one;b=two");
  }

  @Test
  void matrixParam_null() {
    final String url = foo().path("bar").matrixParam("a", null).matrixParam("b", "two")
      .build();

    assertThat(url).isEqualTo("https://foo/bar;b=two");
  }

  @Test
  void matrixParam_path() {
    final String url = foo()
      .path("bar").matrixParam("a", null).matrixParam("b", "two")
      .path("foo")
      .build();

    assertThat(url).isEqualTo("https://foo/bar;b=two/foo");
  }

  @Test
  void param() {
    assertThat(foo().queryParam("bar", null).build()).isEqualTo("https://foo");
    assertThat(foo().queryParam("bar", "a").build()).isEqualTo("https://foo?bar=a");
    assertThat(foo().queryParam("bar", "a").queryParam("baz", "b").build()).isEqualTo("https://foo?bar=a&baz=b");
  }

  @Test
  void param_encode() {
    assertThat(foo().queryParam("some one", "a%b").build()).isEqualTo("https://foo?some+one=a%25b");
  }

  @Test
  void queryParam_when_null() {
    assertThat(foo().queryParam("bar", null).build()).isEqualTo("https://foo");
    assertThat(foo().queryParam("bar", (Boolean) null).build()).isEqualTo("https://foo");
    assertThat(foo().queryParam("bar", (Integer) null).build()).isEqualTo("https://foo");
    assertThat(foo().queryParam("bar", (Long) null).build()).isEqualTo("https://foo");
    assertThat(foo().queryParam("bar", (UUID) null).build()).isEqualTo("https://foo");
    assertThat(foo().queryParam("bar", (LocalTime) null).build()).isEqualTo("https://foo");
    assertThat(foo().queryParam("bar", (LocalDate) null).build()).isEqualTo("https://foo");
    assertThat(foo().queryParam("bar", (LocalDateTime) null).build()).isEqualTo("https://foo");
    assertThat(foo().queryParam("bar", (Instant) null).build()).isEqualTo("https://foo");
  }

  @Test
  void queryParam_when_many() {
    String url = foo()
      .queryParam("a", "a")
      .queryParam("b", true)
      .queryParam("c", false)
      .queryParam("d", 1)
      .queryParam("e", 2L)
      .queryParam("f", LocalTime.of(13, 42))
      .queryParam("g", LocalDate.of(2020, 1, 4))
      .queryParam("h", LocalDateTime.of(2020, 1, 4, 13, 44))
      .build();

    assertThat(url).isEqualTo("https://foo?a=a&b=true&c=false&d=1&e=2&f=13:42&g=2020-01-04&h=2020-01-04T13:44");
  }

  @Test
  void queryParam_when_uuid() {
    UUID uuid = UUID.randomUUID();
    String url = foo()
      .queryParam("a", uuid)
      .build();

    assertThat(url).isEqualTo("https://foo?a=" + uuid);
  }

  @Test
  void queryParam_when_instant() {
    Instant now = Instant.now();
    String url = foo()
      .queryParam("since", now)
      .build();

    assertThat(url).isEqualTo("https://foo?since=" + now.toString());
  }

  @NotNull
  private UrlBuilder foo() {
    return new UrlBuilder("https://foo");
  }

  @Test
  void env() {
    assertThat(UrlBuilder.enc("some one")).isEqualTo("some+one");
    assertThat(UrlBuilder.enc("a%b")).isEqualTo("a%25b");
  }
}
