package org.example.myapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.http.client.BodyReader;
import io.avaje.http.client.BodyWriter;
import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.HttpException;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.http.client.RequestLogger;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.myapp.web.HelloDto;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HelloControllerTest extends BaseWebTest {

  final HttpClientContext clientContext;

  HelloControllerTest() {

    final HttpClient httpClient = HttpClient.newBuilder()
      .build();

    this.clientContext = HttpClientContext.newBuilder()
      .withBaseUrl(baseUrl)
      .withResponseListener(new RequestLogger())
      .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
      .with(httpClient)
      .build();
  }
  @Test
  void hello() {
    final Response response = get(baseUrl + "/hello/message");
    assertThat(response.body().asString()).contains("hello world");
    assertThat(response.statusCode()).isEqualTo(200);

    final HttpResponse<String> hres = clientContext.request().path("hello").path("message")
      .get().asString();

    assertThat(hres.body()).contains("hello world");
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void hello2() {

    TypeRef<List<HelloDto>> listDto = new TypeRef<List<HelloDto>>() { };
    final List<HelloDto> beans = given()
      .get(baseUrl + "/hello")
      .then()
      .statusCode(200)
      .extract()
      .as(listDto);

    assertThat(beans).hasSize(2);

    final List<HelloDto> helloDtos = clientContext.request()
      .path("hello")
      .get().list(HelloDto.class);

    assertThat(helloDtos).hasSize(2);
  }

  @Test
  void getWithPathParamAndQueryParam() {

    final HelloDto bean = given()
      .get(baseUrl + "/hello/43/2020-03-05?otherParam=other")
      .then()
      .statusCode(200)
      .extract()
      .as(HelloDto.class);

    assertThat(bean.id).isEqualTo(43L);
    assertThat(bean.name).isEqualTo("2020-03-05");
    assertThat(bean.otherParam).isEqualTo("other");

    final HelloDto dto = clientContext.request()
      .path("hello/43/2020-03-05").queryParam("otherParam", "other").queryParam("foo", null)
      .get().bean(HelloDto.class);

    assertThat(dto.id).isEqualTo(43L);
    assertThat(dto.name).isEqualTo("2020-03-05");
    assertThat(dto.otherParam).isEqualTo("other");
  }

  @Test
  void postIt() {
    HelloDto dto = new HelloDto(12, "rob", "other");

    given().body(dto).post(baseUrl + "/hello")
      .then()
      .body("id", equalTo(12))
      .body("name", equalTo("posted"))
      .body("otherParam", equalTo("other"));

    final BodyWriter from = clientContext.converters().beanWriter(HelloDto.class);
    final BodyReader<HelloDto> toDto = clientContext.converters().beanReader(HelloDto.class);

    final HelloDto bean = clientContext.request()
      .path("hello")
      .body(from.write(dto))
      .post()
      .read(toDto);

    assertEquals("posted", bean.name);
    assertEquals(12, bean.id);
  }

  @Test
  void saveBean() {
    HelloDto dto = new HelloDto(12, "rob", "other");

    given().body(dto).post(baseUrl + "/hello/savebean/foo")
      .then()
      .statusCode(201);
  }

  @Test
  void postForm_controller_using_formbean() {

    given().urlEncodingEnabled(true)
      .param("name", "Bazz")
      .param("email", "user@foo.com")
      .param("url", "http://foo.com")
      .param("startDate", "2030-12-03")
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

    final HelloDto bean = clientContext.request()
      .path("hello/saveform3")
      .formParam("name", "Bax")
      .formParam("email", "Bax@foo.com")
      .formParam("url", "http://foo.com")
      .formParam("startDate", "2030-12-03")
      .post().bean(HelloDto.class);

    assertThat(bean.name).isEqualTo("Bax");
    assertThat(bean.otherParam).isEqualTo("Bax@foo.com");
    assertThat(bean.id).isEqualTo(52);

    given().urlEncodingEnabled(true)
      .param("name", "Bax")
      .param("email", "Bax@foo.com")
      .param("url", "http://foo.com")
      .param("startDate", "2030-12-03")
      .header("Accept", ContentType.JSON.getAcceptHeader())
      .post(baseUrl + "/hello/saveform3")
      .then()
      .body("name", equalTo("Bax"))
      .body("otherParam", equalTo("Bax@foo.com"))
      .body("id", equalTo(52))
      .statusCode(201);
  }

  @Test
  void postForm_validation_expect_badRequest() {

    final ErrorResponse res = given().urlEncodingEnabled(true)
      .param("email", "user@foo.com")
      .param("url", "notAValidUrl")
      .header("Accept", ContentType.JSON.getAcceptHeader())
      .post(baseUrl + "/hello/saveform")
      .then()
      .statusCode(422)
      .extract()
      .as(ErrorResponse.class);

    assertNotNull(res);
    assertThat(res.getMessage()).contains("failed validation");
    final Map<String, String> errors = res.getErrors();
    assertThat(errors.get("url")).isEqualTo("must be a valid URL");
    assertThat(errors.get("name")).isEqualTo("must not be null");

    try {
      clientContext.request()
        .path("hello/saveform")
        .formParam("email", "user@foo.com")
        .formParam("url", "notAValidUrl")
        .post()
        .asVoid();

    } catch (HttpException e) {
      assertEquals(422, e.getStatusCode());

      final HttpResponse<?> httpResponse = e.getHttpResponse();
      assertNotNull(httpResponse);
      assertEquals(422, httpResponse.statusCode());

      final ErrorResponse errorResponse = e.bean(ErrorResponse.class);

      final Map<String, String> errorMap = errorResponse.getErrors();
      assertThat(errorMap.get("url")).isEqualTo("must be a valid URL");
      assertThat(errorMap.get("name")).isEqualTo("must not be null");

    }
  }

  @Test
  void delete() {
    given().delete(baseUrl + "/hello/52")
      .then()
      .statusCode(204);
  }

  @Test
  void getWithMatrixParam() {
    given()
      .get(baseUrl + "/hello/withMatrix/2011;author=rob;country=nz/foo?extra=banana")
      .then()
      .statusCode(200)
      .body(equalTo("yr:2011 au:rob co:nz other:foo extra:banana"));

    given()
      .get(baseUrl + "/hello/withMatrix/2011;author=rob;country=nz/foo")
      .then()
      .statusCode(200)
      .body(equalTo("yr:2011 au:rob co:nz other:foo extra:null"));

    given()
      .get(baseUrl + "/hello/withMatrix/2011;author=rob/foo2")
      .then()
      .statusCode(200)
      .body(equalTo("yr:2011 au:rob co:null other:foo2 extra:null"));

    given()
      .get(baseUrl + "/hello/withMatrix/2011;country=nz/foo2")
      .then()
      .statusCode(200)
      .body(equalTo("yr:2011 au:null co:nz other:foo2 extra:null"));

    given()
      .get(baseUrl + "/hello/withMatrix/2011/foo3")
      .then()
      .statusCode(200)
      .body(equalTo("yr:2011 au:null co:null other:foo3 extra:null"));


    final HttpResponse<String> httpRes = clientContext.request()
      .path("hello/withMatrix/2011")
      .matrixParam("author", "rob")
      .matrixParam("country", "nz")
      .path("foo")
      .queryParam("extra", "banana")

      .get().asString();

    assertEquals(200, httpRes.statusCode());
    assertEquals("yr:2011 au:rob co:nz other:foo extra:banana", httpRes.body());
  }
}
