package io.avaje.http.client;

/**
 * Provides http client implementations for an interface.
 *
 * @param <T> The interface type
 */
public interface HttpApiProvider<T> {

  /**
   * Return the interface type this API implements.
   */
  Class<T> type();

  /**
   * Return the provided implementation of the API.
   */
  T provide(HttpClientContext client);

}
