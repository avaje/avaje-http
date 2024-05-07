package org.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.http.client.HttpClient;

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
  void maybeNoContent() {
    HttpResponse<String> res = client.request()
      .path("test/maybeNoContent")
      .queryParam("empty", false)
      .GET()
      .asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("Hi");

    HttpResponse<String> res2 = client.request()
      .path("test/maybeNoContent")
      .queryParam("empty", true)
      .GET()
      .asString();

    assertThat(res2.statusCode()).isEqualTo(204);
    assertThat(res2.body()).isEqualTo("");
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

  @Test
  void testNoBodyResponse() {
    HttpResponse<Person> res = client.request()
      .path("test/maybe/true")
      .GET().as(Person.class);

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body().name()).isEqualTo("hi");

    HttpResponse<Person> resNoBody = client.request()
      .path("test/maybe/false")
      .GET().as(Person.class);

    assertThat(resNoBody.statusCode()).isEqualTo(204);
    assertThat(resNoBody.body()).isNull();
  }

  @Test
  void testNoBodyListResponse() {
    HttpResponse<List<Person>> res = client.request()
      .path("test/maybeList/true")
      .GET().asList(Person.class);

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body().getFirst().name()).isEqualTo("hi");

    HttpResponse<List<Person>> resNoBody = client.request()
      .path("test/maybeList/false")
      .GET().asList(Person.class);

    assertThat(resNoBody.statusCode()).isEqualTo(204);
    assertThat(resNoBody.body()).isEmpty();
  }
}
