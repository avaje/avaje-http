package io.avaje.http.client;

/**
 * Writes beans as content for a specific content type.
 */
public interface BodyWriter<T> {

  /**
   * Write the bean as content using the default content type.
   * <p>
   * Used when all beans sent via POST, PUT, PATCH will be sent as
   * a single content type like <code>application/json; charset=utf8</code>.
   */
  BodyContent write(T bean);

  /**
   * Write the bean as content with the requested content type.
   * <p>
   * The writer is expected to use the given contentType to determine
   * how to write the bean as content.
   */
  BodyContent write(T bean, String contentType);

}
