package io.avaje.http.generator.core.openapi;

public enum MediaType {
  APPLICATION_JSON("application/json"),
  APPLICATION_STREAM_JSON("application/stream+json"),
  TEXT_PLAIN("text/plain"),
  TEXT_HTML("text/html"),
  HTML_UTF8("text/html;charset=UTF8"),
  UNKNOWN("");

  private final String value;

  MediaType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static MediaType parse(String s) {
    for (final MediaType mediaType : values()) {
      if (mediaType.getValue().equalsIgnoreCase(s)) {
        return mediaType;
      }
    }
    return UNKNOWN;
  }
}
