package io.avaje.http.client;

import java.nio.charset.StandardCharsets;

/**
 * Bytes based body content.
 */
final class DBodyContent implements BodyContent {

  private static final String JSON_UTF8 = "application/json; charset=UTF-8";

  private final String contentType;
  private final byte[] content;

  static BodyContent asJson(byte[] content) {
    return new DBodyContent(JSON_UTF8, content);
  }

  DBodyContent(byte[] content) {
    this.content = content;
    this.contentType = null;
  }

  DBodyContent(String contentType, byte[] content) {
    this.contentType = contentType;
    this.content = content;
  }

  @Override
  public String contentType() {
    return contentType;
  }

  @Override
  public byte[] content() {
    return content;
  }

  @Override
  public boolean isEmpty() {
    return content.length == 0;
  }

  @Override
  public String contentAsUtf8() {
    return new String(content, StandardCharsets.UTF_8);
  }
}
