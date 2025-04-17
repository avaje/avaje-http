package io.avaje.http.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthTokenTest {

  private static final ObjectMapper objectMapper = init();

  private static ObjectMapper init() {
    return new ObjectMapper()
      //.registerModule(new JavaTimeModule())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
      .configure(SerializationFeature.INDENT_OUTPUT, true)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  static class MyAuthTokenProvider implements AuthTokenProvider {

    @Override
    public AuthToken obtainToken(HttpClientRequest tokenRequest) {
      AuthTokenResponse res = tokenRequest
        .url("https://foo/v2/token")
        .header("content-type", "application/json")
        .body(authRequestAsJson())
        .POST()
        .bean(AuthTokenResponse.class);

      Instant validUntil = Instant.now().plusSeconds(res.expires_in).minusSeconds(60);

      return AuthToken.of(res.access_token, validUntil);
    }
  }

  @Test
  void expiration() {
    Instant plus = Instant.now().plus(120, ChronoUnit.SECONDS);
    AuthToken authToken = AuthToken.of("foo", plus);

    assertThat(authToken.isExpired()).isFalse();
    assertThat(authToken.expiration().toSeconds()).isBetween(118L, 120L);
  }

  @Disabled
  @Test
  void sendEmail() {

    var ctx = HttpClient.builder()
      .baseUrl("https://foo")
      .bodyAdapter(new JacksonBodyAdapter(objectMapper))
      .authTokenProvider(new MyAuthTokenProvider())
      .build();

    String path = "bar";

    HttpResponse<String> res = ctx.request()
      .path(path)
      .header("Content-Type", "application/json")
      //.body(payload)
      .POST()
      .asString();

    HttpResponse<String> res2 = ctx.request()
      .path(path)
      .header("Content-Type", "application/json")
      //.body(payload)
      .POST()
      .asString();

  }

  private static String authRequestAsJson() {
    return null;
  }

  static public class AuthTokenResponse {
    public String access_token;
    public Long expires_in;
    // ...
  }
}
