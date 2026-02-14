package org.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.http.client.HttpClient;

public class HelloControllerTest {

  private static TestPair pair = new TestPair();
  private static HttpClient client = pair.client();

  @AfterAll
  static void end() {
    pair.stop();
  }

  @Test
  void hello() {
    HttpResponse<String> res = client.request()
      .path("hello")
      .GET()
      .asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("Hello world");
  }

  @Test
  void personList() {
    HttpResponse<List<Person>> res = client.request()
      .path("person/name/list")
      .GET()
      .asList(Person.class);

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).hasSize(2);
  }

  @Test
  void personStream() {
    HttpResponse<String> res = client.request()
      .path("person/ignoreMe/stream")
      .GET()
      .asString(); // .asStream(Person.class);

    assertThat(res.statusCode()).isEqualTo(200);
    String contentType = res.headers().firstValue("Content-Type").orElse("Junk");
    assertThat(res.body()).isEqualTo("{\"id\":42,\"name\":\"ignoreMe\"}\n{\"id\":43,\"name\":\"bar\"}\n{\"id\":44,\"name\":\"baz\"}\n{\"id\":44,\"name\":\"bax\"}\n\n");
    assertThat(contentType).isEqualTo("application/stream+json");
  }

  @Test
  void streamBytesTest() {
    HttpResponse<String> res = client.request().path("streamBytes").GET().asString();

    Optional<String> contentTypeHeaderValueOptional = res.headers().firstValue("Content-Type");

    assertThat(contentTypeHeaderValueOptional.isPresent()).isEqualTo(true);
    assertThat(contentTypeHeaderValueOptional.get()).isEqualTo("text/html");
    assertThat(res.body()).isEqualTo("Avaje");
    assertThat(res.statusCode()).isEqualTo(200);
  }

  @Test
  void deleteOne() {
    HttpResponse<Void> res = client.request()
      .path("delete/one/1")
      .DELETE()
      .asVoid();

    assertThat(res.statusCode()).isEqualTo(204);
  }

  @Test
  void deleteTwo() {
    HttpResponse<String> res = client.request()
      .path("delete/two/1")
      .DELETE()
      .asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("deleteTwo:1");
  }
}
