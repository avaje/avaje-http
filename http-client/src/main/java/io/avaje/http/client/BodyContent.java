package io.avaje.http.client;

/**
 * Content of request or response body used for adapting to beans.
 * <p>
 * This is not used for streaming content.
 */
public class BodyContent {

  public static final String JSON_UTF8 = "application/json; charset=UTF-8";

  private final String contentType;

  private final byte[] content;

  /**
   * Create and return as JSON body content given raw content.
   */
  public static BodyContent asJson(byte[] content) {
    return new BodyContent(JSON_UTF8, content);
  }

  /**
   * Create with content type and content.
   */
  public BodyContent(String contentType, byte[] content) {
    this.contentType = contentType;
    this.content = content;
  }

  /**
   * Return the content type.
   */
  public String contentType() {
    return contentType;
  }

  /**
   * Return the raw content.
   */
  public byte[] content() {
    return content;
  }
}
