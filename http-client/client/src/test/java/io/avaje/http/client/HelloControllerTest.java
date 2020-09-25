package io.avaje.http.client;

import org.example.webserver.ErrorResponse;
import org.example.webserver.HelloDto;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class HelloControllerTest extends BaseWebTest {

  final HttpClientContext clientContext = client();

  @Test
  void get_helloMessage() {

    final HttpResponse<String> hres = clientContext.request()
      .path("hello").path("message")
      .get().asString();

    assertThat(hres.body()).contains("hello world");
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void get_hello_returningListOfBeans() {

    final List<HelloDto> helloDtos = clientContext.request()
      .path("hello")
      .get().list(HelloDto.class);

    assertThat(helloDtos).hasSize(2);
  }

  @Test
  void get_withPathParamAndQueryParam_returningBean() {

    final HelloDto dto = clientContext.request()
      .path("hello/43/2020-03-05").queryParam("otherParam", "other").queryParam("foo", null)
      .get().bean(HelloDto.class);

    assertThat(dto.id).isEqualTo(43L);
    assertThat(dto.name).isEqualTo("2020-03-05");
    assertThat(dto.otherParam).isEqualTo("other");
  }

  @Test
  void post_bean_returningBean_usingExplicitConverters() {

    HelloDto dto = new HelloDto(12, "rob", "other");

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
  void post_bean_returningVoid() {

    HelloDto dto = new HelloDto(12, "rob", "other");

    final HttpResponse<Void> res = clientContext.request()
      .path("hello/savebean/foo")
      .body(dto).post()
      .asDiscarding();

    assertThat(res.statusCode()).isEqualTo(201);
  }

  @Test
  void postForm() {

    final HttpResponse<Void> res = clientContext.request()
      .path("hello/saveform")
      .formParam("name", "Bazz")
      .formParam("email", "user@foo.com")
      .formParam("url", "http://foo.com")
      .formParam("startDate", "2020-12-03")
      .post()
      .asDiscarding();

    assertThat(res.statusCode()).isEqualTo(201);
  }

  @Test
  void postForm_returningBean() {

    final HttpResponse<Void> res = clientContext.request()
      .path("hello/saveform")
      .formParam("name", "Bazz")
      .formParam("email", "user@foo.com")
      .formParam("url", "http://foo.com")
      .formParam("startDate", "2020-12-03")
      .post()
      .asDiscarding();

    assertThat(res.statusCode()).isEqualTo(201);

    final HelloDto bean = clientContext.request()
      .path("hello/saveform3")
      .formParam("name", "Bax")
      .formParam("email", "Bax@foo.com")
      .formParam("url", "http://foo.com")
      .formParam("startDate", "2020-12-03")
      .post()
      .bean(HelloDto.class);

    assertThat(bean.name).isEqualTo("Bax");
    assertThat(bean.otherParam).isEqualTo("Bax@foo.com");
    assertThat(bean.id).isEqualTo(52);
  }

  @Test
  void postForm_asVoid_invokesValidation_expect_badRequest_extractError() {

    try {
      clientContext.request()
        .path("hello/saveform")
        .formParam("email", "user@foo.com")
        .formParam("url", "notAValidUrl")
        .post()
        .asVoid();

      fail();

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
  void postForm_asBytes_validation_expect_badRequest_extractError() {

    try {
      clientContext.request()
        .path("hello/saveform")
        .formParam("email", "user@foo.com")
        .formParam("url", "notAValidUrl")
        .post().asVoid();

      fail();

    } catch (HttpException e) {
      assertEquals(422, e.getStatusCode());

      final HttpResponse<?> httpResponse = e.getHttpResponse();
      assertNotNull(httpResponse);
      assertEquals(422, httpResponse.statusCode());

      final ErrorResponse errorResponse = e.bean(ErrorResponse.class);
      final Map<String, String> errorMap = errorResponse.getErrors();
      assertThat(errorMap.get("url")).isEqualTo("must be a valid URL");
      assertThat(errorMap.get("name")).isEqualTo("must not be null");

      String rawBody = e.bodyAsString();
      assertThat(rawBody).contains("must be a valid URL");

      final byte[] rawBytes = e.bodyAsBytes();
      assertThat(rawBytes).isNotNull();
    }
  }

  @Test
  void delete() {
    final HttpResponse<Void> res =
      clientContext.request()
        .path("hello/52")
        .delete().asDiscarding();

    assertThat(res.statusCode()).isEqualTo(204);
  }

  @Test
  void get_withMatrixParam() {

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
