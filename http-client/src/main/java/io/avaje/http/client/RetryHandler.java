package io.avaje.http.client;

import java.net.http.HttpResponse;

/**
 * Define how retry should occur on a request.
 */
public interface RetryHandler {

  /**
   * Return true if the request should be retried.
   *
   * @param retryCount The number of retry attempts already executed
   * @param response The HTTP response
   * @return True if the request should be retried or false if not
   */
  boolean isRetry(int retryCount, HttpResponse<?> response);

  /**
   * Return true if the request should be retried.
   *
   * @param retryCount The number of retry attempts already executed
   * @param exception The Wrapped Error thrown by the underlying Http Client
   * @return True if the request should be retried or false if not
   */
  default boolean isExceptionRetry(int retryCount, HttpException exception) {
    throw exception;
  }
}
