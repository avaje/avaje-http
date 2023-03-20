package org.example;

import io.avaje.http.client.HttpClient;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class MainTest {

  @Inject
  static HttpClient httpClient;

  @Test
  void one() {
    HttpResponse<String> res = httpClient.request()
      .GET().asString();

    assertThat(res.body()).isEqualTo("hello world");
  }

  @Test
  void oneBean() {
    HelloController.Something bean = httpClient.request()
      .path("one")
      .GET().bean(HelloController.Something.class);

    assertThat(bean.id()).isEqualTo(52);
    assertThat(bean.name()).isEqualTo("Asdasd");
  }

  @Test
  void fooBean() {
    FooController.Foo bean = httpClient.request()
      .path("foo")
      .GET().bean(FooController.Foo.class);

    assertThat(bean.id()).isEqualTo(82);
    assertThat(bean.name()).isEqualTo("Foo here");
  }

  @Test
  void health() {
    HttpResponse<String> res = httpClient.request()
      .path("health")
      .GET().asString();

    assertThat(res.body()).isEqualTo("ok");
  }

  @Test
  void two() {
    HttpResponse<String> res = httpClient.request()
      .GET().asString();

    assertThat(res.body()).isEqualTo("hello world");
  }

  @Test
  void three() {
    HttpResponse<String> res = httpClient.request()
      .GET().asString();

    assertThat(res.body()).isEqualTo("hello world");
  }
}
