package org.example.myapp;

import org.example.myapp.web.Bar;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

class BarControllerTest extends BaseWebTest {

  @Test
  void getBars() {
    given()
      .get(baseUrl + "/bars")
      .then()
      .statusCode(200)
      .body(equalTo("Hello"));
  }

  @Test
  void getById() {

    Bar bar = given()
      .get(baseUrl + "/bars/53")
      .then()
      .statusCode(200)
      .extract()
      .as(Bar.class);

    assertThat(bar.id).isEqualTo(53L);
    assertThat(bar.name).isEqualTo("Rob53");
  }

  @Test
  void findByCode() {
    given()
      .get(baseUrl + "/bars/find/mycode")
      .then()
      .statusCode(200);
  }

}
