package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/** Extends DHttpClientRequest with retry attempts. */
final class DHttpClientRequestWithRetry extends DHttpClientRequest {

  private final RetryHandler retryHandler;
  private int retryCount;

  DHttpClientRequestWithRetry(
      DHttpClientContext context, Duration requestTimeout, RetryHandler retryHandler) {
    super(context, requestTimeout);
    this.retryHandler = retryHandler;
    isRetry = true;
  }

  /** Perform send with retry. */
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

  /** Perform send with retry. */
  @Override
  protected <T> CompletableFuture<HttpResponse<T>> performSendAsync(
      boolean loggable, HttpResponse.BodyHandler<T> responseHandler) {

    var resultFuture = asyncwithRetry(loggable, responseHandler);

    if (errorMapper != null) {
      resultFuture =
          resultFuture.handle(
              (r, e) -> {
                if (e != null && e.getCause() instanceof HttpException) {
                  throw errorMapper.apply((HttpException) e.getCause());
                }
                return r;
              });
    }
    return resultFuture;
  }

  protected boolean retry(HttpResponse<?> res, HttpException ex) {

    if (res != null) {
      return retryHandler.isRetry(retryCount, res);
    }
    return retryHandler.isExceptionRetry(retryCount, ex);
  }

  private <T> CompletableFuture<HttpResponse<T>> asyncwithRetry(
      boolean loggable, HttpResponse.BodyHandler<T> responseHandler) {

    return super.performSendAsync(loggable, responseHandler)
        .handle(
            (res, ex) -> {
              if (ex != null && ex.getCause() instanceof HttpException) {
                if (!retryHandler.isExceptionRetry(retryCount, (HttpException) ex.getCause()))
                  return CompletableFuture.<HttpResponse<T>>failedFuture(ex.getCause());
                retryCount++;
                return asyncwithRetry(loggable, responseHandler);
              }

              if (res != null && res.statusCode() < 300) {
                return CompletableFuture.completedFuture(res);
              }
              if (!retryHandler.isRetry(retryCount, res)) {
                return CompletableFuture.<HttpResponse<T>>failedFuture(ex.getCause());
              }
              retryCount++;
              return asyncwithRetry(loggable, responseHandler);
            })
        .thenCompose(Function.identity());
  }
}
