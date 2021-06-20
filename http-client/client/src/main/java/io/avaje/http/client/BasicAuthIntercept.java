package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Adds Basic Authorization header to requests.
 */
public class BasicAuthIntercept implements RequestIntercept {

  private final String headerValue;

  /**
   * Construct with the username and password.
   */
  public BasicAuthIntercept(String username, String password) {
    this.headerValue = "Basic "+encode(username, password);
  }

  /**
   * Return Base64 encoding of {@literal username:password}
   */
  public static String encode(String username, String password) {
    return Base64.getEncoder().encodeToString((username + ":" + password).getBytes(UTF_8));
  }

  @Override
  public void beforeRequest(HttpClientRequest request) {
    request.header("Authorization", headerValue);
  }

  @Override
  public void afterResponse(HttpResponse<?> response, HttpClientRequest request) {
    // do nothing
  }
}
