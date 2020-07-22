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
}
