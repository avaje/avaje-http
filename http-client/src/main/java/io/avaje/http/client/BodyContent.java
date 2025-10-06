package io.avaje.http.client;

/**
 * Content of request or response body.
 * <p>
 * This is not used for streaming content.
 */
public interface BodyContent {

  /**
   * Create BodyContent with the given byte[] content.
   */
  static BodyContent of(byte[] content) {
    return new DBodyContent(content);
  }

  /**
   * Create BodyContent with the given string content.
   */
  static BodyContent of(String content) {
    return new DBodyContentS(null, content);
  }

  /**
   * Create BodyContent with the given the content type and string content.
   */
  static BodyContent of(String contentType, String content) {
    return new DBodyContentS(contentType, content);
  }

  /**
   * Create BodyContent with the given the content type and byte[] content.
   */
  static BodyContent of(String contentType, byte[] content) {
    return new DBodyContent(contentType, content);
  }

  /**
   * Create BodyContent for JSON byte[] content.
   */
  static BodyContent asJson(byte[] content) {
    return DBodyContent.asJson(content);
  }

  /**
   * Return the content type.
   */
  String contentType();

  /**
   * Return the content as bytes.
   */
  byte[] content();

  /**
   * Return the content as UTF8 string.
   */
  String contentAsUtf8();

  /**
   * Return true if the content is empty.
   */
  boolean isEmpty();
}
