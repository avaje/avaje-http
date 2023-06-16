package io.avaje.http.client;

/**
 * Provides http client implementations for an interface.
 *
 * @param <T> The interface type
 */
@FunctionalInterface
public interface HttpApiProvider<T> {

  /** Return the provided implementation of the API. */
  T provide(HttpClient client);
}
