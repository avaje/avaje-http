package io.avaje.http.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Build a URL typically using a base url and adding path and query parameters.
 */
public interface UrlBuilder {

  /**
   * URL encode the value.
   */
  static String enc(String val) {
    return URLEncoder.encode(val, StandardCharsets.UTF_8);
  }

  /**
   * Create a UrlBuilder with a base url.
   */
  static UrlBuilder of(String baseUrl) {
    return new DUrlBuilder(baseUrl);
  }

  /**
   * Set the url. This effectively replaces a base url.
   */
  UrlBuilder url(String url);

  /**
   * Add a path segment to the url.
   * <p>
   * This includes appending a "/" prefix with the path.
   */
  UrlBuilder path(String path);

  /**
   * Add a path segment to the url.
   */
  UrlBuilder path(int val);

  /**
   * Add a path segment to the url.
   */
  UrlBuilder path(long val);

  /**
   * Add a path segment to the url.
   */
  UrlBuilder path(Object val);

  /**
   * Append a query parameter.
   * <p>
   * The name and value parameters are url encoded.
   */
  UrlBuilder queryParam(String name, String value);

  /**
   * Append a query parameter.
   * <p>
   * The name and value parameters are url encoded.
   */
  UrlBuilder queryParam(String name, Object value);

  /**
   * Append a query parameters.
   */
  UrlBuilder queryParam(Map<String, ?> params);

  /**
   * Append a matrix parameter.
   * <p>
   * The name and value parameters are url encoded.
   */
  UrlBuilder matrixParam(String name, String value);

  /**
   * Append a matrix parameter.
   * <p>
   * The name and value parameters are url encoded.
   */
  UrlBuilder matrixParam(String name, Object value);

  /**
   * Return the full URL.
   */
  String build();
}
