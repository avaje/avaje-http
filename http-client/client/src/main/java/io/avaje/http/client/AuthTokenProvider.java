package io.avaje.http.client;

/**
 * Use to obtain an Authorization bearer token that is expected to be used.
 *
 * <pre>{@code
 *
 *   class MyAuthTokenProvider implements AuthTokenProducer {
 *
 *     @Override
 *     public AuthToken obtainToken(HttpClientRequest tokenRequest) {
 *
 *       MyTokenResponse tokenResponse = tokenRequest
 *         .url("https://foo/auth/token")
 *         .header("content-type", "application/json")
 *         .body(authRequestAsJson())
 *         .post()
 *         .bean(MyTokenResponse.class);
 *
 *       String token = tokenResponse.getToken();
 *       long expiresSecs = tokenResponse.getExpiresInSecs();
 *
 *       Instant validUntil = Instant.now().plusSeconds(expiresSecs).minusSeconds(60);
 *
 *       return AuthToken.of(token, validUntil);
 *     }
 *   }
 *
 * }</pre>
 */
public interface AuthTokenProvider {

  /**
   * Obtain a new Authorization token.
   *
   * @param tokenRequest A new request to obtain an Authorisation token
   */
  AuthToken obtainToken(HttpClientRequest tokenRequest);

}
