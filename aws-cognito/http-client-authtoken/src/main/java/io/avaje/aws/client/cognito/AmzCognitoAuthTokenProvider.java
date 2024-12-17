package io.avaje.aws.client.cognito;

import io.avaje.http.client.AuthToken;
import io.avaje.http.client.AuthTokenProvider;
import io.avaje.http.client.BasicAuthIntercept;
import io.avaje.http.client.HttpClientRequest;
import io.avaje.json.simple.SimpleMapper;

import java.net.http.HttpResponse;
import java.time.Instant;

final class AmzCognitoAuthTokenProvider implements CognitoAuthTokenProvider.Builder {

  private String url;
  private String clientId;
  private String clientSecret;
  private String scope;

  @Override
  public CognitoAuthTokenProvider.Builder url(String url) {
    this.url = url;
    return this;
  }

  @Override
  public CognitoAuthTokenProvider.Builder clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  @Override
  public CognitoAuthTokenProvider.Builder clientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
    return this;
  }

  @Override
  public CognitoAuthTokenProvider.Builder scope(String scope) {
    this.scope = scope;
    return this;
  }

  @Override
  public AuthTokenProvider build() {
    return new Provider(url, clientId, clientSecret, scope);
  }

  private static final class Provider implements AuthTokenProvider {

    private static final SimpleMapper MAPPER = SimpleMapper.builder().build();

    private final String url;
    private final String clientId;
    private final String scope;
    private final String authHeader;

    public Provider(String url, String clientId, String clientSecret, String scope) {
      this.url = url;
      this.clientId = clientId;
      this.scope = scope;
      this.authHeader = "Basic " + BasicAuthIntercept.encode(clientId, clientSecret);
    }

    @Override
    public AuthToken obtainToken(HttpClientRequest request) {
      HttpResponse<String> res = request
        .url(url)
        .header("Authorization", authHeader)
        .formParam("grant_type", "client_credentials")
        .formParam("client_id", clientId)
        .formParam("scope", scope)
        .POST()
        .asString();

      if (res.statusCode() != 200) {
        throw new IllegalStateException("Error response getting access token statusCode:" + res.statusCode() + " res:" + res);
      }
      return decodeAuthToken(res.body());
    }

    private AuthToken decodeAuthToken(String responseBody) {
      final var responseMap = MAPPER.fromJsonObject(responseBody);
      final var accessToken = (String) responseMap.get("access_token");
      final var expiresIn = (Long) responseMap.get("expires_in");

      var validUntil = Instant.now()
        .plusSeconds(expiresIn)
        .minusSeconds(60);

      return AuthToken.of(accessToken, validUntil);
    }
  }
}
