package io.avaje.http.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;

/**
 * Adds Basic Authorization header to requests.
 */
public final class BasicAuthIntercept implements RequestIntercept {

  private final String headerValue;

  /**
   * Construct with the username and password.
   */
  public BasicAuthIntercept(String username, String password) {
    this.headerValue = header(username, password);
  }

  /**
   * Return the Basic header value with the encoding of {@literal username:password}
   * <p>
   * Provided as a helper method for code that wants the header value to then
   * apply explicitly rather than using this as a request intercept.
   *
   * @return {@code "Basic " + encode(username, password)}
   */
  public static String header(String username, String password) {
    return "Basic " + encode(username, password);
  }

  /**
   * Return Base64 encoding of {@literal username:password}
   * <p>
   * Provided as a helper method for code that wants to encode the username
   * password pair and use that explicitly rather than using this as a request intercept.
   */
  public static String encode(String username, String password) {
    return Base64.getEncoder().encodeToString((username + ":" + password).getBytes(UTF_8));
  }

  @Override
  public void beforeRequest(HttpClientRequest request) {
    request.header("Authorization", headerValue);
  }
}
