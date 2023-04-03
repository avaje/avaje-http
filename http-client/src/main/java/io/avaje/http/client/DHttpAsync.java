package io.avaje.http.client;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

final class DHttpAsync implements HttpAsyncResponse {

  private final DHttpClientRequest request;

  DHttpAsync(DHttpClientRequest request) {
    this.request = request;
  }

  private <E> CompletableFuture<HttpResponse<E>> with(boolean loggable, HttpResponse.BodyHandler<E>  handler) {
    return request
      .performSendAsync(loggable, handler)
      .thenApply(request::afterAsync);
  }

  @Override
  public <E> CompletableFuture<HttpResponse<E>> handler(HttpResponse.BodyHandler<E>  handler) {
    return with(false, handler);
  }

  @Override
  public CompletableFuture<HttpResponse<Void>> asDiscarding() {
    return with(false, HttpResponse.BodyHandlers.discarding());
  }

  @Override
  public CompletableFuture<HttpResponse<Void>> asVoid() {
    // read the response content as bytes so that it is available for error response
    return request
      .performSendAsync(true, HttpResponse.BodyHandlers.ofByteArray())
      .thenApply(request::asyncVoid);
  }

  @Override
  public CompletableFuture<HttpResponse<String>> asString() {
    return with(true, HttpResponse.BodyHandlers.ofString());
  }

  @Override
  public CompletableFuture<HttpResponse<byte[]>> asByteArray() {
    return with(true, HttpResponse.BodyHandlers.ofByteArray());
  }

  @Override
  public CompletableFuture<HttpResponse<Stream<String>>> asLines() {
    return with(false, HttpResponse.BodyHandlers.ofLines());
  }

  @Override
  public CompletableFuture<HttpResponse<InputStream>> asInputStream() {
    return with(false, HttpResponse.BodyHandlers.ofInputStream());
  }

  @Override
  public <E> CompletableFuture<E> bean(Class<E> type) {
    return as(type).thenApply(HttpResponse::body);
  }

  @Override
  public <E> CompletableFuture<E> bean(Type type) {
    final CompletableFuture<HttpResponse<E>> future = as(type);
    return future.thenApply(HttpResponse::body);
  }

  @Override
  public <E> CompletableFuture<HttpResponse<E>> as(Class<E> type) {
    return asyncAsBytes().thenApply(httpResponse -> request.asyncBean(type, httpResponse));
  }

  @Override
  public <E> CompletableFuture<HttpResponse<E>> as(Type type) {
    return asyncAsBytes().thenApply(httpResponse -> request.asyncBean(type, httpResponse));
  }

  @Override
  public <E> CompletableFuture<List<E>> list(Class<E> type) {
    return asList(type).thenApply(HttpResponse::body);
  }

  @Override
  public <E> CompletableFuture<List<E>> list(Type type) {
    final CompletableFuture<HttpResponse<List<E>>> future = asList(type);
    return future.thenApply(HttpResponse::body);
  }

  @Override
  public <E> CompletableFuture<HttpResponse<List<E>>> asList(Class<E> type) {
    return asyncAsBytes().thenApply(httpResponse -> request.asyncList(type, httpResponse));
  }

  @Override
  public <E> CompletableFuture<HttpResponse<List<E>>> asList(Type type) {
    return asyncAsBytes().thenApply(httpResponse -> request.asyncList(type, httpResponse));
  }

  @Override
  public <E> CompletableFuture<Stream<E>> stream(Class<E> type) {
    return asStream(type).thenApply(HttpResponse::body);
  }

  @Override
  public <E> CompletableFuture<Stream<E>> stream(Type type) {
    final CompletableFuture<HttpResponse<Stream<E>>> future = asStream(type);
    return future.thenApply(HttpResponse::body);
  }

  @Override
  public <E> CompletableFuture<HttpResponse<Stream<E>>> asStream(Class<E> type) {
    return asyncAsLines().thenApply(httpResponse -> request.asyncStream(type, httpResponse));
  }

  @Override
  public <E> CompletableFuture<HttpResponse<Stream<E>>> asStream(Type type) {
    return asyncAsLines().thenApply(httpResponse -> request.asyncStream(type, httpResponse));
  }

  private CompletableFuture<HttpResponse<byte[]>> asyncAsBytes() {
    return request.performSendAsync(true, HttpResponse.BodyHandlers.ofByteArray());
  }

  private CompletableFuture<HttpResponse<Stream<String>>> asyncAsLines() {
    return request.performSendAsync(false, HttpResponse.BodyHandlers.ofLines());
  }
}
