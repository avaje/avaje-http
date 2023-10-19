package io.avaje.http.client;

import java.util.Collection;
import java.util.Map;

final class DUrlBuilder implements UrlBuilder {

  private final StringBuilder buffer = new StringBuilder(100);

  private boolean hasParams;

  DUrlBuilder(String base) {
    buffer.append(base);
  }

  @Override
  public UrlBuilder url(String url) {

    if (url.startsWith("http") && url.contains("://")) {
      buffer.setLength(0);
    }

    buffer.append(url);
    return this;
  }

  @Override
  public UrlBuilder path(String path) {
    buffer.append("/").append(path);
    return this;
  }

  @Override
  public UrlBuilder path(int val) {
    return path(Integer.toString(val));
  }

  @Override
  public UrlBuilder path(long val) {
    return path(Long.toString(val));
  }

  @Override
  public UrlBuilder path(Object val) {
    return path(val.toString());
  }

  private void addQueryParam(String name, String safeValue) {
    buffer.append(hasParams ? '&' : '?');
    hasParams = true;
    buffer.append(UrlBuilder.enc(name)).append("=").append(safeValue);
  }

  @Override
  public UrlBuilder queryParam(String name, String value) {
    if (value != null) {
      addQueryParam(name, UrlBuilder.enc(value));
    }
    return this;
  }

  @Override
  public UrlBuilder queryParam(String name, Object value) {
    if (value instanceof Collection) {
      for (var e : (Collection<?>) value) {
        queryParam(name, e);
      }
      return this;
    }

    if (value != null) {
      addQueryParam(name, value.toString());
    }
    return this;
  }

  @Override
  public UrlBuilder queryParam(Map<String, ?> params) {
    if (params != null) {
      for (Map.Entry<String, ?> entry : params.entrySet()) {
        queryParam(entry.getKey(), entry.getValue());
      }
    }
    return this;
  }

  @Override
  public UrlBuilder matrixParam(String name, String value) {
    if (value != null) {
      buffer.append(';').append(UrlBuilder.enc(name)).append("=").append(UrlBuilder.enc(value));
    }
    return this;
  }

  @Override
  public UrlBuilder matrixParam(String name, Object value) {
    if (value != null) {
      buffer.append(';').append(UrlBuilder.enc(name)).append("=").append(UrlBuilder.enc(value.toString()));
    }
    return this;
  }

  @Override
  public String build() {
    return buffer.toString();
  }

}
