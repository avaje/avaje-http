package io.avaje.http.client;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

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

}
