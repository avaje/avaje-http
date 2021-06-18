package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Async responses as CompletableFuture.
 */
public interface HttpAsyncResponse {

  /**
   * Process discarding response body as {@literal HttpResponse<Void>}.
   */
  CompletableFuture<HttpResponse<Void>> asDiscarding();

  /**
   * Process as String response body {@literal HttpResponse<String>}.
   */
  CompletableFuture<HttpResponse<String>> asString();

}
