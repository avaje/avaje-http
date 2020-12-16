package org.example.web;

import io.avaje.http.client.HttpClientContext;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloControllerTest extends BaseWebTest {

  final static HttpClientContext client = client();

  @Test
  void getHello() {
    final HelloDto hello = client.request().get().bean(HelloDto.class);

    assertEquals(42, hello.id);
    assertEquals("rob", hello.name);
  }

  @Test
  void getPlain() {

    final HttpResponse<String> res = client.request().path("plain").get().asString();

    assertEquals("something", res.body());
    assertThat(res.headers().firstValue("content-type").get()).startsWith("text/plain;");
  }

  @Test
  void getName() {

    assertEquals("hi bazz", client.request().path("other/bazz").get().asString().body());
    assertEquals("hi bax", client.request().path("other/bax").get().asString().body());
  }

  @Test
  void splat() {
    assertEquals("got name:one splat0:a/b splat1:x/y/z", client.request().path("splat/one/a/b/other/x/y/z").get().asString().body());
  }
}
