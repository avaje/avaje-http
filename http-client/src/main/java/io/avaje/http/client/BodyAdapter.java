package io.avaje.http.client;

import java.lang.reflect.Type;
import java.util.List;

/** Adapter between beans and content of a request or response. */
public interface BodyAdapter {

  /**
   * BodyWriter to write beans of this type as request content.
   *
   * @param type The type of the bean this writer is for
   */
  <T> BodyWriter<T> beanWriter(Class<?> type);

  /**
   * BodyWriter to write beans of this type as request content.
   *
   * @param type The type of the bean this writer is for
   */
  default <T> BodyWriter<T> beanWriter(Type type) {
    throw new UnsupportedOperationException("java.lang.reflect.Type is not supported for this adapter");
  }

  /**
   * BodyReader to read response content and convert to a bean.
   *
   * @param type type to convert the content to.
   */
  <T> BodyReader<T> beanReader(Class<T> type);

  /**
   * BodyReader to read response content and convert to a bean.
   *
   * @param type type to convert the content to.
   */
  default <T> BodyReader<T> beanReader(Type type) {
    throw new UnsupportedOperationException("java.lang.reflect.Type is not supported for this adapter");
  }

  /**
   * BodyReader to read response content and convert to a list of beans.
   *
   * @param type type to convert the content to.
   */
  <T> BodyReader<List<T>> listReader(Class<T> type);

  /**
   * BodyReader to read response content and convert to a list of beans.
   *
   * @param type type to convert the content to.
   */
  default <T> BodyReader<List<T>> listReader(Type type) {
    throw new UnsupportedOperationException("java.lang.reflect.Type is not supported for this adapter");
  }
}
