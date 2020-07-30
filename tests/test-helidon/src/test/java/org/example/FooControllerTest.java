package org.example;

import io.restassured.response.Response;
import org.example.api.FooBody;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

class FooControllerTest extends BaseWebTest {

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


  @Test
  void getWithMatrixParam() {
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
