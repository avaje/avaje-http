package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Extends DHttpClientRequest with retry attempts.
 */
class DHttpClientRequestWithRetry extends DHttpClientRequest {

  private final RetryHandler retryHandler;
  private int retryCount;

  DHttpClientRequestWithRetry(DHttpClientContext context, Duration requestTimeout, RetryHandler retryHandler) {
    super(context, requestTimeout);
    this.retryHandler = retryHandler;
  }

  /**
   * Perform send with retry.
   */
  @Override
  protected <T> HttpResponse<T> performSend(HttpResponse.BodyHandler<T> responseHandler) {
    HttpResponse<T> res;
    res = super.performSend(responseHandler);
    if (res.statusCode() < 300) {
      return res;
    }
    while (retryHandler.isRetry(retryCount++, res)) {
      res = super.performSend(responseHandler);
    }
    return res;
  }

}
