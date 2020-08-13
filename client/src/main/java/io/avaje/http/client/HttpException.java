package io.avaje.http.client;

import java.net.http.HttpResponse;

public class HttpException extends RuntimeException {

  private final int statusCode;

  private HttpResponse<?> httpResponse;
  private byte[] rawBody;

  public HttpException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  public HttpException(int statusCode, String message, Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
  }

  public HttpException(int statusCode, Throwable cause) {
    super(cause);
    this.statusCode = statusCode;
  }

  HttpClientContext context;

  public HttpException(HttpResponse<?> httpResponse, HttpClientContext context) {
    super();
    this.httpResponse = httpResponse;
    this.context = context;
    this.statusCode = httpResponse.statusCode();
  }

  HttpException(HttpClientContext context, HttpResponse<byte[]> httpResponse) {
    super();
    this.httpResponse = httpResponse;
    this.context = context;
    this.statusCode = httpResponse.statusCode();
  }

  @SuppressWarnings("unchecked")
  public <T> T bean(Class<T> cls) {
    final BodyContent body = context.readContent((HttpResponse<byte[]>) httpResponse);
    return context.converters().beanReader(cls).read(body);
  }

  public int getStatusCode() {
    return statusCode;
  }

  public HttpResponse<?> getHttpResponse() {
    return httpResponse;
  }
}
