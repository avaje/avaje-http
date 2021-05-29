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
   * @param response   The HTTP response
   * @return True if the request should be retried or false if not
   */
  boolean isRetry(int retryCount, HttpResponse<?> response);

}
