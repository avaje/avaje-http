package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

class DHttpAsync implements HttpAsyncResponse {

  private final DHttpClientRequest request;

  DHttpAsync(DHttpClientRequest request) {
    this.request = request;
  }

  @Override
  public <E> CompletableFuture<HttpResponse<E>> withHandler(HttpResponse.BodyHandler<E>  handler) {
    return request
      .performSendAsync(false, handler)
      .thenApply(request::afterAsync);
  }

  @Override
  public CompletableFuture<HttpResponse<Void>> asDiscarding() {
    return withHandler(HttpResponse.BodyHandlers.discarding());
  }

  @Override
  public CompletableFuture<HttpResponse<String>> asString() {
    return request
      .performSendAsync(true, HttpResponse.BodyHandlers.ofString())
      .thenApply(request::afterAsync);
  }

  @Override
  public <E> CompletableFuture<E> bean(Class<E> type) {
    return request
      .performSendAsync(true, HttpResponse.BodyHandlers.ofByteArray())
      .thenApply(httpResponse -> request.asyncBean(type, httpResponse));
  }

  @Override
  public <E> CompletableFuture<List<E>> list(Class<E> type) {
    return request
      .performSendAsync(true, HttpResponse.BodyHandlers.ofByteArray())
      .thenApply(httpResponse -> request.asyncList(type, httpResponse));
  }

  @Override
  public <E> CompletableFuture<Stream<E>> stream(Class<E> type) {
    return request
      .performSendAsync(false, HttpResponse.BodyHandlers.ofLines())
      .thenApply(httpResponse -> request.asyncStream(type, httpResponse));
  }
}
