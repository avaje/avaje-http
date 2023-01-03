package org.example.myapp;

import org.example.myapp.web.Baz;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

class BazControllerTest extends BaseWebTest {

  @Test
  void findAll() {
    given()
      .get(baseUrl + "/baz")
      .then()
      .statusCode(200);
  }

  @Test
  void findById() {

    Baz baz = given()
      .get(baseUrl + "/baz/53")
      .then()
      .statusCode(200)
      .extract()
      .as(Baz.class);

    assertThat(baz.id).isEqualTo(53L);
    assertThat(baz.name).isEqualTo("Baz53");
  }

  @Test
  void searchByName() {
    given()
      .get(baseUrl + "/baz/findbyname/mycode")
      .then()
      .statusCode(200);
  }

}
