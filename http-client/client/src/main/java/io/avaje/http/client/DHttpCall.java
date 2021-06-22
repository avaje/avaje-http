package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

class DHttpCall implements HttpCallResponse {

  private final DHttpClientRequest request;

  DHttpCall(DHttpClientRequest request) {
    this.request = request;
  }

  @Override
  public HttpCall<HttpResponse<Void>> asDiscarding() {
    return new CallDiscarding();
  }

  @Override
  public HttpCall<HttpResponse<String>> asString() {
    return new CallString();
  }

  @Override
  public <E> HttpCall<HttpResponse<E>> withHandler(HttpResponse.BodyHandler<E> bodyHandler) {
    return new CallHandler<>(bodyHandler);
  }

  @Override
  public <E> HttpCall<E> bean(Class<E> type) {
    return new CallBean<>(type);
  }

  @Override
  public <E> HttpCall<List<E>> list(Class<E> type) {
    return new CallList<>(type);
  }

  @Override
  public <E> HttpCall<Stream<E>> stream(Class<E> type) {
    return new CallStream<>(type);
  }

  private class CallDiscarding implements HttpCall<HttpResponse<Void>> {
    @Override
    public HttpResponse<Void> execute() {
      return request.asVoid();
    }
    @Override
    public CompletableFuture<HttpResponse<Void>> async() {
      return request.async().asDiscarding();
    }
  }

  private class CallString implements HttpCall<HttpResponse<String>> {
    @Override
    public HttpResponse<String> execute() {
      return request.asString();
    }
    @Override
    public CompletableFuture<HttpResponse<String>> async() {
      return request.async().asString();
    }
  }

  private class CallBean<E> implements HttpCall<E> {
    private final Class<E> type;
    CallBean(Class<E> type) {
      this.type = type;
    }
    @Override
    public E execute() {
      return request.bean(type);
    }
    @Override
    public CompletableFuture<E> async() {
      return request.async().bean(type);
    }
  }

  private class CallList<E> implements HttpCall<List<E>> {
    private final Class<E> type;
    CallList(Class<E> type) {
      this.type = type;
    }
    @Override
    public List<E> execute() {
      return request.list(type);
    }
    @Override
    public CompletableFuture<List<E>> async() {
      return request.async().list(type);
    }
  }

  private class CallStream<E> implements HttpCall<Stream<E>> {
    private final Class<E> type;
    CallStream(Class<E> type) {
      this.type = type;
    }
    @Override
    public Stream<E> execute() {
      return request.stream(type);
    }
    @Override
    public CompletableFuture<Stream<E>> async() {
      return request.async().stream(type);
    }
  }

  private class CallHandler<E> implements HttpCall<HttpResponse<E>> {
    private final HttpResponse.BodyHandler<E> handler;
    CallHandler(HttpResponse.BodyHandler<E> handler) {
      this.handler = handler;
    }
    @Override
    public HttpResponse<E> execute() {
      return request.withHandler(handler);
    }
    @Override
    public CompletableFuture<HttpResponse<E>> async() {
      return request.async().withHandler(handler);
    }
  }
}
