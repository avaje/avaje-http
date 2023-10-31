package org.example;

import io.avaje.http.api.Post;
import io.avaje.http.api.Valid;
import io.avaje.http.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

class TestControllerTest {

  private static TestPair pair = new TestPair();
  private static HttpClient client = pair.client();

  @AfterAll
  static void end() {
    pair.stop();
  }

  @Test
  void hello() {
    HttpResponse<String> res = client.request()
      .GET()
      .asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("Hello world - index");
  }

  @Test
  void strBody() {
    HttpResponse<String> res = client.request()
      .path("test/strBody")
      .body("{\"key\":42}")
      .POST()
      .asString();

    assertThat(res.statusCode()).isEqualTo(201);
    assertThat(res.body()).isEqualTo("{\"key\":42}");
    assertThat(res.headers().firstValue("Content-Type")).isPresent().get().isEqualTo("application/json");
    assertThat(res.headers().firstValue("Content-Length")).isPresent();
  }

  @Test
  void strBody3() {
    HttpResponse<String> res = client.request()
      .path("test/strBody3")
      .body("{\"key\":42}")
      .POST()
      .asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("{\"hi\":\"strBody3\"}");
    assertThat(res.headers().firstValue("Content-Type")).isPresent().get().isEqualTo("application/json");
    assertThat(res.headers().firstValue("Content-Length")).isPresent();
  }

  @Test
  void blah() {
    HttpResponse<String> res = client.request()
      .path("test/blah")
      .POST()
      .asString();

    assertThat(res.statusCode()).isEqualTo(202);
    assertThat(res.body()).isEqualTo("{\"hi\":\"yo\",\"level\":42}");
    assertThat(res.headers().firstValue("Content-Type")).isPresent().get().isEqualTo("application/json");
    assertThat(res.headers().firstValue("Content-Length")).isPresent();
  }

  @Test
  void ithrowRuntimeException() {
    HttpResponse<String> res = client.request()
      .path("ithrowRuntimeException")
      .GET()
      .asString();

    assertThat(res.statusCode()).isEqualTo(407);
  }

  @Test
  void ithrowException() {
    HttpResponse<String> res = client.request()
      .path("ithrowException")
      .GET()
      .asString();

    assertThat(res.statusCode()).isEqualTo(501);
    assertThat(res.headers().firstValue("X-Foo").orElse("")).isEqualTo("WasHere");
  }

  @Test
  void ithrowIllegalStateException() {
    HttpResponse<String> res = client.request()
      .path("ithrowIllegalStateException")
      .GET()
      .asString();

    assertThat(res.statusCode()).isEqualTo(503);
  }

  @Valid(groups=MyForm2.class)
  @Post("formBean2")
  String formBean(MyForm2 form) {
    return form.name + "|" + form.email + "|" + form.url;
  }
}
