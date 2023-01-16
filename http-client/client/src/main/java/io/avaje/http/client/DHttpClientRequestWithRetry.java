package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Extends DHttpClientRequest with retry attempts.
 */
final class DHttpClientRequestWithRetry extends DHttpClientRequest {

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
    HttpException ex;

    do {
      try {
        res = super.performSend(responseHandler);
        ex = null;
      } catch (final HttpException e) {
        ex = e;
        res = null;
      }
      if (res != null && res.statusCode() < 300) {
        return res;
      }
      retryCount++;
    } while (retry(res, ex));

    if (res == null && ex != null) {
      throw ex;
    }

    return res;
  }

  protected boolean retry(HttpResponse<?> res, HttpException ex) {

    if (res != null) {
      return retryHandler.isRetry(retryCount, res);
    }
    return retryHandler.isExceptionRetry(retryCount, ex);
  }
}
