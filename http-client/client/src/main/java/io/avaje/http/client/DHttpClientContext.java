package io.avaje.http.client;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

class DHttpClientContext implements HttpClientContext {

  /**
   * HTTP Authorization header.
   */
  static final String AUTHORIZATION = "Authorization";
  private static final String BEARER = "Bearer ";

  private final HttpClient httpClient;
  private final String baseUrl;
  private final Duration requestTimeout;
  private final BodyAdapter bodyAdapter;
  private final RequestListener requestListener;
  private final RequestIntercept requestIntercept;
  private final RetryHandler retryHandler;
  private final boolean withAuthToken;
  private final AuthTokenProvider authTokenProvider;
  private final AtomicReference<AuthToken> tokenRef = new AtomicReference<>();
  private int loggingMaxBody = 1_000;

  DHttpClientContext(HttpClient httpClient, String baseUrl, Duration requestTimeout, BodyAdapter bodyAdapter, RetryHandler retryHandler, RequestListener requestListener, AuthTokenProvider authTokenProvider, RequestIntercept intercept) {
    this.httpClient = httpClient;
    this.baseUrl = baseUrl;
    this.requestTimeout = requestTimeout;
    this.bodyAdapter = bodyAdapter;
    this.retryHandler = retryHandler;
    this.requestListener = requestListener;
    this.authTokenProvider = authTokenProvider;
    this.withAuthToken = authTokenProvider != null;
    this.requestIntercept = intercept;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T create(Class<T> clientInterface) {
    if (!clientInterface.isInterface()) {
      throw new IllegalArgumentException("API declarations must be interfaces.");
    }
    HttpApiProvider<T> apiProvider = DHttpApi.get(clientInterface);
    if (apiProvider != null) {
      return apiProvider.provide(this);
    }
    String implClassName = clientImplementationClassName(clientInterface);
    try {
      Class<?> serviceClass = Class.forName(implClassName);
      Constructor<?> constructor = serviceClass.getConstructor(HttpClientContext.class);
      Object service = constructor.newInstance(this);
      return (T) service;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create http client service " + implClassName, e);
    }
  }

  private <T> String clientImplementationClassName(Class<T> clientInterface) {
    String packageName = clientInterface.getPackageName();
    String simpleName = clientInterface.getSimpleName();
    return packageName + ".httpclient." + simpleName + "$HttpClient";
  }

  @Override
  public HttpClientRequest request() {
    return retryHandler == null
      ? new DHttpClientRequest(this, requestTimeout)
      : new DHttpClientRequestWithRetry(this, requestTimeout, retryHandler);
  }

  @Override
  public BodyAdapter converters() {
    return bodyAdapter;
  }

  @Override
  public UrlBuilder url() {
    return new UrlBuilder(baseUrl);
  }

  @Override
  public HttpClient httpClient() {
    return httpClient;
  }

  @Override
  public void checkResponse(HttpResponse<?> response) {
    if (response.statusCode() >= 300) {
      throw new HttpException(response, this);
    }
  }

  void checkMaybeThrow(HttpResponse<byte[]> response) {
    if (response.statusCode() >= 300) {
      throw new HttpException(this, response);
    }
  }

  @SuppressWarnings("unchecked")
  public BodyContent readErrorContent(boolean responseAsBytes, HttpResponse<?> httpResponse) {
    if (responseAsBytes) {
      return readContent((HttpResponse<byte[]>) httpResponse);
    }
    final String contentType = getContentType(httpResponse);
    final Object body = httpResponse.body();
    if (body instanceof String) {
      return new BodyContent(contentType, ((String) body).getBytes(StandardCharsets.UTF_8));
    }
    String type = (body == null) ? "null" : body.getClass().toString();
    throw new IllegalStateException("Unable to translate response body to bytes? Maybe use HttpResponse directly instead?  Response body type: " + type);
  }

  @Override
  public BodyContent readContent(HttpResponse<byte[]> httpResponse) {
    byte[] bodyBytes = decodeContent(httpResponse);
    final String contentType = getContentType(httpResponse);
    return new BodyContent(contentType, bodyBytes);
  }

  String getContentType(HttpResponse<?> httpResponse) {
    return firstHeader(httpResponse.headers(), "Content-Type", "content-type");
  }

  String getContentEncoding(HttpResponse<?> httpResponse) {
    return firstHeader(httpResponse.headers(), "Content-Encoding", "content-encoding");
  }

  @Override
  public byte[] decodeContent(String encoding, byte[] body) {
    if (encoding.equals("gzip")) {
      return GzipUtil.gzipDecode(body);
    }
    // todo: register decoders with context and use them
    return body;
  }

  public byte[] decodeContent(HttpResponse<byte[]> httpResponse) {
    String encoding = getContentEncoding(httpResponse);
    return encoding == null ? httpResponse.body() : decodeContent(encoding, httpResponse.body());
  }

  String firstHeader(HttpHeaders headers, String... names) {
    final Map<String, List<String>> map = headers.map();
    for (String key : names) {
      final List<String> values = map.get(key);
      if (values != null && !values.isEmpty()) {
        return values.get(0);
      }
    }
    return null;
  }

  <T> HttpResponse<T> send(HttpRequest.Builder requestBuilder, HttpResponse.BodyHandler<T> bodyHandler) {
    try {
      return httpClient.send(requestBuilder.build(), bodyHandler);
    } catch (IOException e) {
      throw new HttpException(499, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new HttpException(499, e);
    }
  }

  <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest.Builder requestBuilder, HttpResponse.BodyHandler<T> bodyHandler) {
    return httpClient.sendAsync(requestBuilder.build(), bodyHandler);
  }

  <T> BodyContent write(T bean, String contentType) {
    return bodyAdapter.beanWriter(bean.getClass()).write(bean, contentType);
  }

  <T> BodyReader<T> beanReader(Class<T> cls) {
    return bodyAdapter.beanReader(cls);
  }

  <T> T readBean(Class<T> cls, BodyContent content) {
    return bodyAdapter.beanReader(cls).read(content);
  }

  <T> List<T> readList(Class<T> cls, BodyContent content) {
    return bodyAdapter.listReader(cls).read(content);
  }

  void afterResponse(DHttpClientRequest request) {
    if (requestListener != null) {
      requestListener.response(request.listenerEvent());
    }
    if (requestIntercept != null) {
      requestIntercept.afterResponse(request.response(), request);
    }
  }

  void beforeRequest(DHttpClientRequest request) {
    if (withAuthToken && !request.isSkipAuthToken()) {
      request.header(AUTHORIZATION, BEARER + authToken());
    }
    if (requestIntercept != null) {
      requestIntercept.beforeRequest(request);
    }
  }

  private String authToken() {
    AuthToken authToken = tokenRef.get();
    if (authToken == null || authToken.isExpired()) {
      authToken = authTokenProvider.obtainToken(request().skipAuthToken());
      tokenRef.set(authToken);
    }
    return authToken.token();
  }

  String maxResponseBody(String body) {
    return body.length() > loggingMaxBody ? body.substring(0, loggingMaxBody) + " <truncated> ..." : body;
  }
}
