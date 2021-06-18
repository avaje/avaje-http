package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Async responses as CompletableFuture.
 */
public interface HttpAsyncResponse {

  /**
   * Process discarding response body as {@literal HttpResponse<Void>}.
   *
   * <pre>{@code
   *
   *   clientContext.request()
   *       .path("hello/world")
   *       .GET()
   *       .async().asDiscarding()
   *       .whenComplete((hres, throwable) -> {
   *
   *         if (throwable != null) {
   *           ...
   *         } else {
   *           int statusCode = hres.statusCode();
   *           ...
   *         }
   *       });
   *
   * }</pre>
   */
  CompletableFuture<HttpResponse<Void>> asDiscarding();

  /**
   * Process as String response body {@literal HttpResponse<String>}.
   *
   * <pre>{@code
   *
   *   clientContext.request()
   *       .path("hello/world")
   *       .GET()
   *       .async().asString()
   *       .whenComplete((hres, throwable) -> {
   *
   *         if (throwable != null) {
   *           ...
   *         } else {
   *           int statusCode = hres.statusCode();
   *           String body = hres.body();
   *           ...
   *         }
   *       });
   *
   * }</pre>
   */
  CompletableFuture<HttpResponse<String>> asString();

  /**
   * Process expecting a bean response body (typically from json content).
   * <p>
   * If the HTTP statusCode is 300 or above a HttpException is throw which
   * contains the HttpResponse.
   *
   * <pre>{@code
   *
   *    clientContext.request()
   *       ...
   *       .POST().async()
   *       .bean(HelloDto.class)
   *       .whenComplete((helloDto, throwable) -> {
   *
   *         if (throwable != null) {
   *           HttpException httpException = (HttpException) throwable.getCause();
   *           int statusCode = httpException.getStatusCode();
   *
   *           // maybe convert json error response body to a bean (using Jackson/Gson)
   *           MyErrorBean errorResponse = httpException.bean(MyErrorBean.class);
   *           ..
   *
   *         } else {
   *           // use helloDto
   *           ...
   *         }
   *
   *       });
   *
   *
   * }</pre>
   */
  <E> CompletableFuture<E> bean(Class<E> type);
}
