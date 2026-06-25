package org.example.myapp;

import io.avaje.http.client.HttpClient;
import org.example.myapp.web.Bar;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.List;

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

  @Test
  void search() {
    // The interface with @QueryParam @Nullable String code (TYPE_USE @Nullable);
    // the BarController @Override omits @Nullable. The route must still be generated.
    HttpClient client = client();
    HttpResponse<List<Bar>> res = client.request()
      .path("bars").path("search").path("items")
      .queryParam("code", "abc")
      .GET()
      .asList(Bar.class);

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEmpty();
  }

}
