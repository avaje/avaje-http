package org.example.web;

import io.avaje.http.client.HttpClientContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloControllerTest extends BaseWebTest {

  final static HttpClientContext client = client();

  @Test
  void getHello() {
    final HelloDto hello = client.request().get().bean(HelloDto.class);

    assertEquals(42, hello.id);
    assertEquals("rob", hello.name);
  }
}
