package io.avaje.http.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlBuilder {

  private final StringBuilder buffer = new StringBuilder(100);

  boolean hasParams;

  public UrlBuilder(String base) {
    buffer.append(base);
  }

  public UrlBuilder path(String path) {
    buffer.append("/").append(path);
    return this;
  }

  public UrlBuilder param(String name, String value) {
    if (value != null) {
      buffer.append(hasParams ? '&' : '?');
      hasParams = true;
      buffer.append(enc(name)).append("=").append(enc(value));
    }
    return this;
  }

  public UrlBuilder matrixParam(String name, String value) {
    if (value != null) {
      buffer.append(';').append(enc(name)).append("=").append(enc(value));
    }
    return this;
  }

  public static String enc(String val) {
    return URLEncoder.encode(val, StandardCharsets.UTF_8);
  }

  public String build() {
    return buffer.toString();
  }


}
