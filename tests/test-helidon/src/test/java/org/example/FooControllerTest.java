package org.example;

import io.avaje.http.api.Get;
import io.avaje.http.client.HttpClient;
import io.restassured.response.Response;
import org.example.api.FooBody;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

class FooControllerTest extends BaseWebTest {

  private final HttpClient client = client();

  @Test
  void hello() {
    final Response response = get(baseUrl + "/foo/hello");
    assertThat(response.body().asString()).contains("Hello from Foo");
    assertThat(response.statusCode()).isEqualTo(200);
  }


  @Test
  void hello2() {
    given().get(baseUrl + "/foo/hello")
      .then()
      .statusCode(200)
      .body(equalTo("Hello from Foo"));
  }

  @Test
  void postIt() {
    FooBody fb = new FooBody();
    fb.setMessage("hello");
    fb.name = "testName";
    fb.age = 45;

    given().body(fb).post(baseUrl + "/foo")
      .then()
      .body("age", equalTo(45))
      .body("name", startsWith("testName=hello"));
  }


  @Get("/withMatrix/:year;author;country/:other")
  @Test
  void getWithMatrixParam() {

    final HttpResponse<String> res = client.request()
      .path("foo")
      .path("withMatrix")
      .path("2011").matrixParam("author", "rob").matrixParam("country", "nz")
      .path("foo")
      .queryParam("extra", "banana")
      .GET()
      .asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("yr:2011 au:rob co:nz other:foo extra:banana");

    given()
      .get(baseUrl + "/foo/withMatrix/2011;author=rob;country=nz/foo?extra=banana")
      .then()
      .statusCode(200)
      .body(equalTo("yr:2011 au:rob co:nz other:foo extra:banana"));

    given()
      .get(baseUrl + "/foo/withMatrix/2011;author=rob;country=nz/foo")
      .then()
      .statusCode(200)
      .body(equalTo("yr:2011 au:rob co:nz other:foo extra:null"));

    given()
      .get(baseUrl + "/foo/withMatrix/2011;author=rob/foo2")
      .then()
      .statusCode(200)
      .body(equalTo("yr:2011 au:rob co:null other:foo2 extra:null"));

    given()
      .get(baseUrl + "/foo/withMatrix/2011;country=nz/foo2")
      .then()
      .statusCode(200)
      .body(equalTo("yr:2011 au:null co:nz other:foo2 extra:null"));

    given()
      .get(baseUrl + "/foo/withMatrix/2011/foo3")
      .then()
      .statusCode(200)
      .body(equalTo("yr:2011 au:null co:null other:foo3 extra:null"));

  }
}
