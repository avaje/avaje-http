package io.avaje.http.generator.core.openapi;

public enum MediaType {
  APPLICATION_JSON("application/json"),
  TEXT_PLAIN("text/plain"),
  TEXT_HTML("text/html"),
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
