package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

/** Extends DHttpClientRequest with retry attempts. */
final class DHttpClientRequestWithRetry extends DHttpClientRequest {

  private final RetryHandler retryHandler;

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

    prepareExecution();
    var resultFuture = asyncwithRetry(loggable, responseHandler);

    if (errorMapper != null) {
      resultFuture =
          resultFuture.handle(
              (r, e) -> {
                if (e != null) {
                  final Throwable error = unwrapFutureError(e);
                  if (error instanceof HttpException) {
                    throw errorMapper.apply((HttpException) error);
                  }
                  if (error instanceof RuntimeException) {
                    throw (RuntimeException) error;
                  }
                  throw new CompletionException(error);
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
              if (ex != null) {
                final Throwable error = unwrapFutureError(ex);
                if (error instanceof HttpException) {
                  if (!retryHandler.isExceptionRetry(retryCount, (HttpException) error)) {
                    return CompletableFuture.<HttpResponse<T>>failedFuture(error);
                  }
                  retryCount++;
                  return asyncwithRetry(loggable, responseHandler);
                }
                return CompletableFuture.<HttpResponse<T>>failedFuture(error);
              }

              if (res != null && res.statusCode() < 300) {
                return CompletableFuture.completedFuture(res);
              }
              if (!retryHandler.isRetry(retryCount, res)) {
                return CompletableFuture.completedFuture(res);
              }
              retryCount++;
              return asyncwithRetry(loggable, responseHandler);
            })
        .thenCompose(Function.identity());
  }
}
