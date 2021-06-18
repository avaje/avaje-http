package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

class DHttpAsync implements HttpAsyncResponse {

  private final DHttpClientRequest request;

  DHttpAsync(DHttpClientRequest request) {
    this.request = request;
  }

  @Override
  public CompletableFuture<HttpResponse<Void>> asDiscarding() {
    return request
      .performSendAsync(false, HttpResponse.BodyHandlers.discarding())
      .thenApply(request::afterAsync);
  }

  @Override
  public CompletableFuture<HttpResponse<String>> asString() {
    return request
      .performSendAsync(true, HttpResponse.BodyHandlers.ofString())
      .thenApply(request::afterAsync);
  }

}
