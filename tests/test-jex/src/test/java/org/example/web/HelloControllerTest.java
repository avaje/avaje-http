package org.example.web;

import io.avaje.http.client.HttpClient;
import io.avaje.http.client.HttpException;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HelloControllerTest extends BaseWebTest {

  final static HttpClient client = client();

  @Test
  void getHello() {
    final HelloDto hello = client.request().GET().bean(HelloDto.class);

    assertEquals(42, hello.id);
    assertEquals("rob", hello.name);
  }

  @Test
  void getPlain() {
    final HttpResponse<String> res = client.request().path("plain").GET().asString();

    assertEquals("something", res.body());
    assertThat(res.headers().firstValue("content-type").orElseThrow()).startsWith("text/plain;");
  }

  @Test
  void getName() {
    assertEquals("hi bazz", client.request().path("other/bazz").GET().asString().body());
    assertEquals("hi bax", client.request().path("other/bax").GET().asString().body());
  }

  @Test
  void withDefault() {
    assertEquals("name|bazz;limit|42", client.request().path("withDefault/bazz").GET().asString().body());
    assertEquals("name|bazz;limit|10", client.request().path("withDefault/bazz").queryParam("limit", 10).GET().asString().body());
    assertEquals("name|foo;limit|11", client.request().path("withDefault/foo").queryParam("limit", 11).queryParam("limit", 12).GET().asString().body());
  }

  @Test
  void splat() {
    assertEquals("got name:one splat0:a/b splat1:x/y/z", client.request().path("splat/one/a/b/other/x/y/z").GET().asString().body());
  }

  @Test
  void splat2() {
    assertEquals("got name:one splat0:a/b splat1:x/y/z", client.request().path("splat2/one/a/b/other/x/y/z").GET().asString().body());
  }

  @Test
  void plainText() {
    final HttpResponse<String> hres = client.request()
      .path("bigInt/42")
      .GET().asString();

    assertThat(hres.statusCode()).isEqualTo(200);
    assertThat(hres.body()).contains("hi|42");
  }

  @Test
  void rawJson() {
    final HttpResponse<String> hres = client.request()
      .path("rawJson")
      .GET().asString();

    assertThat(hres.statusCode()).isEqualTo(200);
    assertThat(hres.headers().firstValue("Content-Type").orElseThrow()).isEqualTo("application/json");
    assertThat(hres.body()).contains("{\"key\": 42 }");
  }

  @Test
  void validation() {
    HelloDto helloDto = new HelloDto();
    helloDto.id = 42;

    final HttpResponse<String> hres = client.request()
      .body(helloDto)
      .PUT().asString();

    assertThat(hres.statusCode()).isEqualTo(422);
    assertThat(hres.body()).contains("{\"path\":\"name\",\"field\":\"name\",\"message\":\"must not be null\"}");
  }

  @Test
  void validation_expect_HttpException() {
    HelloDto helloDto = new HelloDto();
    helloDto.id = 42;

    final HttpException ex = assertThrows(HttpException.class, () ->
      client.request()
        .body(helloDto)
        .PUT().asVoid());

    assertThat(ex.statusCode()).isEqualTo(422);

    final ErrorResponse errBean = ex.bean(ErrorResponse.class);
    assertThat(errBean.get("name")).isEqualTo("must not be null");
  }
}
