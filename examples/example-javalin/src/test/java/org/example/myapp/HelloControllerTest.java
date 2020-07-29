package org.example.myapp;

import io.restassured.response.Response;
import org.example.myapp.web.HelloDto;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

class HelloControllerTest extends BaseWebTest {

  @Test
  void hello() {
    final Response response = get(baseUrl + "/hello/message");
    assertThat(response.body().asString()).contains("hello world");
    assertThat(response.statusCode()).isEqualTo(200);
  }

//  @Test
//  void hello2() {
//    given().get(baseUrl + "/foo/hello")
//      .then()
//      .statusCode(200)
//      .body(equalTo("Hello from Foo"));
//  }
//
  @Test
  void postIt() {
    HelloDto dto = new HelloDto(12, "rob", "other");

    given().body(dto).post(baseUrl + "/savebean/bar")
      .then()
      .body("age", equalTo(45))
      .body("name", startsWith("testName=hello"));
  }
}
