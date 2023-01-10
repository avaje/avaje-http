package org.example;

import io.avaje.http.client.HttpClientContext;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class MainTest {

  @Inject
  static HttpClientContext httpClient;

//  @Bean
//  HttpRouting.Builder builder = HttpRouting.builder().get("/hi", (req, res) -> {
//    res.send("hi");
//  });
  @Test
  void one() {
    HttpResponse<String> res = httpClient.request()
      .GET().asString();

    assertThat(res.body()).isEqualTo("hello world");
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
