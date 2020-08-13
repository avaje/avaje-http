package io.avaje.http.client;

public class BodyContent {

  public static final String JSON_UTF8 = "application/json; charset=UTF-8";

  private final String contentType;

  private final byte[] content;

  public static BodyContent asJson(byte[] content) {
    return new BodyContent(JSON_UTF8, content);
  }

  public BodyContent(String contentType, byte[] content) {
    this.contentType = contentType;
    this.content = content;
  }

  public String contentType() {
    return contentType;
  }

  public byte[] content() {
    return content;
  }
}
