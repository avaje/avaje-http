package io.avaje.http.client;

/**
 * Adds a Bearer authentication Authorization header to requests.
 */
public final class BearerTokenIntercept implements RequestIntercept {

  private final String headerValue;

  public BearerTokenIntercept(String token) {
    this.headerValue = "Bearer " + token;
  }

  @Override
  public void beforeRequest(HttpClientRequest request) {
    request.header("Authorization", headerValue);
  }

}
