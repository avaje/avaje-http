package io.avaje.http.client;

import java.util.List;

/**
 * Adaptor between beans and content of a request or response.
 * <p>
 * Typically converts between beans as JSON content.
 */
public interface BodyAdapter {

  /**
   * Return a BodyWriter to write beans of this type as request content.
   *
   * @param type The type of the bean this writer is for
   */
  <T> BodyWriter<T> beanWriter(Class<?> type);

  /**
   * Return a BodyReader to read response content and convert to a bean.
   *
   * @param type The bean type to convert the content to.
   */
  <T> BodyReader<T> beanReader(Class<T> type);

  /**
   * Return a BodyReader to read response content and convert to a list of beans.
   *
   * @param type The bean type to convert the content to.
   */
  <T> BodyReader<List<T>> listReader(Class<T> type);

}
