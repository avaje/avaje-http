package io.avaje.http.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * Build a URL typically using a base url and adding path and query parameters.
 */
public final class UrlBuilder {

  private final StringBuilder buffer = new StringBuilder(100);

  private boolean hasParams;

  /**
   * Create with a base url.
   */
  public UrlBuilder(String base) {
    buffer.append(base);
  }

  /**
   * Set the url. This effectively replaces a base url.
   */
  public UrlBuilder url(String url) {
    buffer.delete(0, buffer.length());
    buffer.append(url);
    return this;
  }

  /**
   * Add a path segment to the url.
   * <p>
   * This includes appending a "/" prefix with the path.
   */
  public UrlBuilder path(String path) {
    buffer.append("/").append(path);
    return this;
  }

  /**
   * Add a path segment to the url.
   */
  public UrlBuilder path(int val) {
    return path(Integer.toString(val));
  }

  /**
   * Add a path segment to the url.
   */
  public UrlBuilder path(long val) {
    return path(Long.toString(val));
  }

  /**
   * Add a path segment to the url.
   */
  public UrlBuilder path(Object val) {
    return path(val.toString());
  }

  private void addQueryParam(String name, String safeValue) {
    buffer.append(hasParams ? '&' : '?');
    hasParams = true;
    buffer.append(enc(name)).append("=").append(safeValue);
  }

  /**
   * Append a query parameter.
   * <p>
   * The name and value parameters are url encoded.
   */
  public UrlBuilder queryParam(String name, String value) {
    if (value != null) {
      addQueryParam(name, enc(value));
    }
    return this;
  }

  /**
   * Append a query parameter.
   * <p>
   * The name and value parameters are url encoded.
   */
  public UrlBuilder queryParam(String name, Object value) {
	  
    if (value instanceof Collection) {
      for (var e : (Collection) value) {
        queryParam(name, e);
      }
      return this;
    }
    
    if (value != null) {
      addQueryParam(name, value.toString());
    }
    return this;
  }

  /**
   * Append a query parameters.
   */
  public UrlBuilder queryParam(Map<String, ?> params) {
    if (params != null) {
      for (Map.Entry<String, ?> entry : params.entrySet()) {
        queryParam(entry.getKey(), entry.getValue());
      }
    }
    return this;
  }

  /**
   * Append a matrix parameter.
   * <p>
   * The name and value parameters are url encoded.
   */
  public UrlBuilder matrixParam(String name, String value) {
    if (value != null) {
      buffer.append(';').append(enc(name)).append("=").append(enc(value));
    }
    return this;
  }

  /**
   * Append a matrix parameter.
   * <p>
   * The name and value parameters are url encoded.
   */
  public UrlBuilder matrixParam(String name, Object value) {
    if (value != null) {
      buffer.append(';').append(enc(name)).append("=").append(enc(value.toString()));
    }
    return this;
  }

  /**
   * URL encode the value.
   */
  public static String enc(String val) {
    return URLEncoder.encode(val, StandardCharsets.UTF_8);
  }

  /**
   * Return the full URL.
   */
  public String build() {
    return buffer.toString();
  }

}
