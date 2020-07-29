package org.example.myapp;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.myapp.web.HelloDto;
import org.junit.jupiter.api.Test;

import java.util.List;

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

  @SuppressWarnings("unchecked")
  @Test
  void hello2() {

    TypeRef listDto = new TypeRef<List<HelloDto>>() { };
    final List<HelloDto> beans =
      (List<HelloDto>)given().get(baseUrl + "/hello")
      .then()
      .statusCode(200)
      .extract()
      .as(listDto);

    assertThat(beans).hasSize(2);
  }

  @Test
  void postIt() {
    HelloDto dto = new HelloDto(12, "rob", "other");

    given().body(dto).post(baseUrl + "/savebean/bar")
      .then()
      .body("age", equalTo(45))
      .body("name", startsWith("testName=hello"));
  }

  @Test
  void postForm_controller_using_formbean() {

    given().urlEncodingEnabled(true)
      .param("name", "Bazz")
      .param("email", "user@foo.com")
      .param("url", "http://foo.com")
      .param("startDate", "2020-12-03")
      .header("Accept", ContentType.JSON.getAcceptHeader())
      .post(baseUrl + "/hello/saveform")
      .then()
      .statusCode(201);

  }

  @Test
  void postForm2_controllerUsingParams() {

    given().urlEncodingEnabled(true)
      .param("name", "Bazz")
      .param("email", "user@foo.com")
      .param("url", "http://foo.com")
      .param("startDate", "2020-12-03")
      .header("Accept", ContentType.JSON.getAcceptHeader())
      .post(baseUrl + "/hello/saveform2")
      .then()
      .statusCode(201);
  }

  @Test
  void postForm3_controllerFormBean_responseJsonDto() {

    given().urlEncodingEnabled(true)
      .param("name", "Bax")
      .param("email", "Bax@foo.com")
      .param("url", "http://foo.com")
      .param("startDate", "2020-12-03")
      .header("Accept", ContentType.JSON.getAcceptHeader())
      .post(baseUrl + "/hello/saveform3")
      .then()
      .body("name", equalTo("Bax"))
      .body("otherParam", equalTo("Bax@foo.com"))
      .body("id", equalTo(52))
      .statusCode(201);
  }

  @Test
  void delete() {
    given().delete(baseUrl + "/hello/52")
      .then()
      .statusCode(204);
  }
}
