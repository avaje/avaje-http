package io.avaje.http.client;

/**
 * Read content as a java type.
 */
public interface BodyReader<T> {

  /**
   * Read the content returning it as a java type.
   */
  T read(BodyContent content);

}
