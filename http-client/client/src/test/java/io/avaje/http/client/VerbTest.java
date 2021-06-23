package io.avaje.http.client;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class VerbTest extends BaseWebTest {

  private final HttpClientContext clientContext = client();

  @Test
  void post() {

    HttpResponse<String> res = clientContext.request()
      .path("post")
      .POST()
      .asString();

    assertThat(res.body()).isEqualTo("post");

    HttpResponse<String> res2 = clientContext.request()
      .path("post")
      .POST()
      .asString();

    assertThat(res2.body()).isEqualTo("post");
  }

  @Test
  void put() {

    HttpResponse<String> res = clientContext.request()
      .path("put")
      .PUT()
      .asString();

    assertThat(res.body()).isEqualTo("put");

    HttpResponse<String> res2 = clientContext.request()
      .path("put")
      .PUT()
      .asString();

    assertThat(res2.body()).isEqualTo("put");
  }

  @Test
  void patch() {

    HttpResponse<String> res = clientContext.request()
      .path("patch")
      .PATCH().asString();

    assertThat(res.body()).isEqualTo("patch");
  }

  @Disabled
  @Test()
  void trace() {

    HttpResponse<String> res = clientContext.request()
      .path("patch")
      .TRACE().asString();

    assertThat(res.body()).isEqualTo("patch");
  }

  @Test
  void delete() {

    HttpResponse<String> res = clientContext.request()
      .path("delete")
      .DELETE().asString();

    assertThat(res.body()).isEqualTo("delete body[]");
  }

  @Test
  void delete_with_body() {

    HttpResponse<String> res = clientContext.request()
      .path("delete")
      .body("dummy")
      .DELETE().asString();

    assertThat(res.body()).isEqualTo("delete body[dummy]");
  }

  @Test
  void delete_with_body_bytes() {

    HttpResponse<String> res = clientContext.request()
      .path("delete")
      .body("dummyBytes".getBytes(StandardCharsets.UTF_8))
      .DELETE().asString();

    assertThat(res.body()).isEqualTo("delete body[dummyBytes]");
  }

  @Test
  void delete_with_body_BodyPublishers() {

    HttpResponse<String> res = clientContext.request()
      .path("delete")
      .body(HttpRequest.BodyPublishers.ofString("dummyBodyPublishers"))
      .DELETE().asString();

    assertThat(res.body()).isEqualTo("delete body[dummyBodyPublishers]");
  }

  @Test
  void delete_with_body_Path() throws URISyntaxException {

    final URL resource = getClass().getResource("/dummy.txt");
    HttpResponse<String> res = clientContext.request()
      .path("delete")
      .body(Path.of(resource.toURI()))
      .DELETE().asString();

    assertThat(res.body()).isEqualTo("delete body[dummyFileContent]");
  }

  @Test
  void delete_with_body_Path_NotFound_expects_IllegalArgumentException() {

    final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
      clientContext.request()
        .path("delete")
        .body(Path.of(URI.create("file:///file-does-not-exist.txt")))
        .DELETE().asString());

    assertThat(e.getMessage()).startsWith("File not found");
  }

  @Test
  void delete_with_body_InputStream() {
    HttpResponse<String> res = clientContext.request()
        .path("delete")
        .body(() -> getClass().getResourceAsStream("/dummy.txt"))
        .DELETE().asString();

    assertThat(res.body()).isEqualTo("delete body[dummyFileContent]");
  }

  @Test
  void head() {

    HttpResponse<String> res = clientContext.request()
      .path("head")
      .body("dummy")
      .HEAD().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("");
  }

  @Test
  void get() {

    HttpResponse<String> res = clientContext.request()
      .path("get")
      .body("dummy")
      .GET().asString();

    assertThat(res.body()).isEqualTo("get");
  }

  @Test
  void get_BodyHandler_null_expect() {

    HttpResponse<String> res = clientContext.request()
      .path("post")
      .body((HttpRequest.BodyPublisher)null)
      .POST().asString();

    assertThat(res.body()).isEqualTo("post");
  }
}
