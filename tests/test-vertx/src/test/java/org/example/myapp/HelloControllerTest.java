package org.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HelloControllerTest extends BaseWebTest {

  @Test
  void rootHello() throws Exception {
    final var response = get("/");
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("Hello World");
  }

  @Test
  void generatedHello() throws Exception {
    final var response = get("/hello");
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("hello world");
  }
}
