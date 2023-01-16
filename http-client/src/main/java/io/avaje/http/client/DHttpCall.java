package io.avaje.http.client;

import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

final class DHttpCall implements HttpCallResponse {

  private final DHttpClientRequest request;

  DHttpCall(DHttpClientRequest request) {
    this.request = request;
  }

  @Override
  public HttpCall<HttpResponse<Void>> asVoid() {
    return new CallVoid();
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
  public HttpCall<HttpResponse<byte[]>> asByteArray() {
    return new CallBytes();
  }

  @Override
  public HttpCall<HttpResponse<Stream<String>>> asLines() {
    return new CallLines();
  }

  @Override
  public HttpCall<HttpResponse<InputStream>> asInputStream() {
    return new CallInputStream();
  }

  @Override
  public <E> HttpCall<HttpResponse<E>> handler(HttpResponse.BodyHandler<E> bodyHandler) {
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

  @Override
  public <E> HttpCall<E> bean(ParameterizedType type) {
    return new CallBean<>(type);
  }

  @Override
  public <E> HttpCall<List<E>> list(ParameterizedType type) {
    return new CallList<>(type);
  }

  @Override
  public <E> HttpCall<Stream<E>> stream(ParameterizedType type) {
    return new CallStream<>(type);
  }

  private class CallVoid implements HttpCall<HttpResponse<Void>> {
    @Override
    public HttpResponse<Void> execute() {
      return request.asVoid();
    }
    @Override
    public CompletableFuture<HttpResponse<Void>> async() {
      return request.async().asVoid();
    }
  }

  private class CallDiscarding implements HttpCall<HttpResponse<Void>> {
    @Override
    public HttpResponse<Void> execute() {
      return request.asDiscarding();
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

  private class CallBytes implements HttpCall<HttpResponse<byte[]>> {
    @Override
    public HttpResponse<byte[]> execute() {
      return request.asByteArray();
    }
    @Override
    public CompletableFuture<HttpResponse<byte[]>> async() {
      return request.async().asByteArray();
    }
  }

  private class CallLines implements HttpCall<HttpResponse<Stream<String>>> {
    @Override
    public HttpResponse<Stream<String>> execute() {
      return request.asLines();
    }
    @Override
    public CompletableFuture<HttpResponse<Stream<String>>> async() {
      return request.async().asLines();
    }
  }

  private class CallInputStream implements HttpCall<HttpResponse<InputStream>> {
    @Override
    public HttpResponse<InputStream> execute() {
      return request.asInputStream();
    }
    @Override
    public CompletableFuture<HttpResponse<InputStream>> async() {
      return request.async().asInputStream();
    }
  }

  private class CallBean<E> implements HttpCall<E> {
    private final Class<E> type;
    private final ParameterizedType genericType;
    private final boolean isGeneric;

    CallBean(Class<E> type) {
      this.isGeneric = false;
      this.type = type;
      this.genericType = null;
    }

    CallBean(ParameterizedType type) {
      this.isGeneric = true;
      this.type = null;
      this.genericType = type;
    }

    @Override
    public E execute() {
      return isGeneric ? request.bean(genericType) : request.bean(type);
    }

    @Override
    public CompletableFuture<E> async() {
      return isGeneric ? request.async().bean(genericType) : request.async().bean(type);
    }
  }

  private class CallList<E> implements HttpCall<List<E>> {
    private final Class<E> type;
    private final ParameterizedType genericType;
    private final boolean isGeneric;

    CallList(Class<E> type) {
      this.isGeneric = false;
      this.type = type;
      this.genericType = null;
    }

    CallList(ParameterizedType type) {
      this.isGeneric = true;
      this.type = null;
      this.genericType = type;
    }

    @Override
    public List<E> execute() {
      return isGeneric ? request.list(genericType) : request.list(type);
    }

    @Override
    public CompletableFuture<List<E>> async() {
      return isGeneric ? request.async().list(genericType) : request.async().list(type);
    }
  }

  private class CallStream<E> implements HttpCall<Stream<E>> {
    private final Class<E> type;
    private final ParameterizedType genericType;
    private final boolean isGeneric;

    CallStream(Class<E> type) {
      this.isGeneric = false;
      this.type = type;
      this.genericType = null;
    }

    CallStream(ParameterizedType type) {
      this.isGeneric = true;
      this.type = null;
      this.genericType = type;
    }

    @Override
    public Stream<E> execute() {
      return isGeneric ? request.stream(genericType) : request.stream(type);
    }

    @Override
    public CompletableFuture<Stream<E>> async() {
      return isGeneric ? request.async().stream(genericType) : request.async().stream(type);
    }
  }

  private class CallHandler<E> implements HttpCall<HttpResponse<E>> {
    private final HttpResponse.BodyHandler<E> handler;
    CallHandler(HttpResponse.BodyHandler<E> handler) {
      this.handler = handler;
    }
    @Override
    public HttpResponse<E> execute() {
      return request.handler(handler);
    }
    @Override
    public CompletableFuture<HttpResponse<E>> async() {
      return request.async().handler(handler);
    }
  }
}
