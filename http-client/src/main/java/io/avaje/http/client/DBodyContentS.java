package io.avaje.http.client;

import java.nio.charset.StandardCharsets;

/**
 * String based body content.
 */
final class DBodyContentS implements BodyContent {

  private final String contentType;
  private final String content;

  DBodyContentS(String contentType, String content) {
    this.contentType = contentType;
    this.content = content;
  }

  @Override
  public String contentType() {
    return contentType;
  }

  @Override
  public byte[] content() {
    return content.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public String contentAsUtf8() {
    return content;
  }
}
