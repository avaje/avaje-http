package io.avaje.http.client;

import java.util.Map;

/**
 * Provides http client implementations for an interface.
 *
 * @param <T> The interface type
 */
@FunctionalInterface
public interface HttpApiProvider<T> {

  /** Return the interface type this API implements. */
  default Class<T> type() {
    throw new UnsupportedOperationException();
  }

  /** Return the provided implementation of the API. */
  T provide(HttpClient client);
}
