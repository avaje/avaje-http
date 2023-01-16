package io.avaje.http.client;

import java.util.concurrent.CompletableFuture;

/**
 * Allows for executing the request asynchronously or synchronously.
 *
 * @param <E> The type of response
 */
public interface HttpCall<E> {

  /**
   * Execute the request returning the result.
   */
  E execute();

  /**
   * Execute the request asynchronously.
   */
  CompletableFuture<E> async();
}
