package io.avaje.http.client;

import io.avaje.applog.AppLog;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.Logger.Level.WARNING;

final class DHttpClientContext implements HttpClient, SpiHttpClient {

  private static final System.Logger log = AppLog.getLogger("io.avaje.http.client");

  static final String AUTHORIZATION = "Authorization";
  private static final String BEARER = "Bearer ";

  private final java.net.http.HttpClient httpClient;
  private final String baseUrl;
  private final Duration requestTimeout;
  private final BodyAdapter bodyAdapter;
  private final RequestListener requestListener;
  private final RequestIntercept requestIntercept;
  private final RetryHandler retryHandler;
  private final boolean withAuthToken;
  private final AuthTokenProvider authTokenProvider;
  private final AtomicReference<AuthToken> tokenRef = new AtomicReference<>();
  private final AtomicReference<Instant> backgroundRefreshLease = new AtomicReference<>();
  private final Duration backgroundRefreshDuration;

  private final LongAdder metricResTotal = new LongAdder();
  private final LongAdder metricResError = new LongAdder();
  private final LongAdder metricResBytes = new LongAdder();
  private final LongAdder metricResMicros = new LongAdder();
  private final LongAccumulator metricResMaxMicros = new LongAccumulator(Math::max, 0);
  private final Function<HttpException, RuntimeException> errorHandler;

  private boolean closed;

  DHttpClientContext(
      java.net.http.HttpClient httpClient,
      String baseUrl,
      Duration requestTimeout,
      BodyAdapter bodyAdapter,
      RetryHandler retryHandler,
      Function<HttpException, RuntimeException> errorHandler,
      RequestListener requestListener,
      AuthTokenProvider authTokenProvider,
      Duration backgroundRefreshDuration,
      RequestIntercept intercept) {
    this.httpClient = httpClient;
    this.baseUrl = baseUrl;
    this.requestTimeout = requestTimeout;
    this.bodyAdapter = bodyAdapter;
    this.retryHandler = retryHandler;
    this.errorHandler = errorHandler;
    this.requestListener = requestListener;
    this.authTokenProvider = authTokenProvider;
    this.backgroundRefreshDuration = backgroundRefreshDuration;
    this.withAuthToken = authTokenProvider != null;
    this.requestIntercept = intercept;
  }

  @Override
  public <T> T create(Class<T> clientInterface) {
    return DHttpApi.get(clientInterface, this);
  }

  @Override
  public <T> T create(Class<T> clientInterface, ClassLoader classLoader) {
    return DHttpApi.get(clientInterface, this, classLoader);
  }

  @Override
  public HttpClientRequest request() {

    if (closed) {
      throw new IllegalStateException("HttpClient is closed");
    }

    return retryHandler == null
      ? new DHttpClientRequest(this, requestTimeout)
      : new DHttpClientRequestWithRetry(this, requestTimeout, retryHandler);
  }

  @Override
  public BodyAdapter bodyAdapter() {
    return bodyAdapter;
  }

  @Override
  public UrlBuilder url() {
    return UrlBuilder.of(baseUrl);
  }

  public Function<HttpException, RuntimeException> errorMapper() {
    return errorHandler;
  }

  @Override
  public java.net.http.HttpClient httpClient() {
    return httpClient;
  }

  @Override
  public HttpClient.Metrics metrics() {
    return metrics(false);
  }

  @Override
  public HttpClient.Metrics metrics(boolean reset) {
    if (reset) {
      return new DMetrics(metricResTotal.sumThenReset(), metricResError.sumThenReset(), metricResBytes.sumThenReset(), metricResMicros.sumThenReset(), metricResMaxMicros.getThenReset());
    } else {
      return new DMetrics(metricResTotal.sum(), metricResError.sum(), metricResBytes.sum(), metricResMicros.sum(), metricResMaxMicros.get());
    }
  }

  void metricsString(int stringBody) {
    metricResBytes.add(stringBody);
  }

  static final class DMetrics implements HttpClient.Metrics {

    private final long totalCount;
    private final long errorCount;
    private final long responseBytes;
    private final long totalMicros;
    private final long maxMicros;

    DMetrics(long totalCount, long errorCount, long responseBytes, long totalMicros, long maxMicros) {
      this.totalCount = totalCount;
      this.errorCount = errorCount;
      this.responseBytes = responseBytes;
      this.totalMicros = totalMicros;
      this.maxMicros = maxMicros;
    }

    @Override
    public String toString() {
      return "totalCount:" + totalCount + " errorCount:" + errorCount + " responseBytes:" + responseBytes + " totalMicros:" + totalMicros + " avgMicros:" + avgMicros()+ " maxMicros:" + maxMicros;
    }

    @Override
    public long totalCount() {
      return totalCount;
    }

    @Override
    public long errorCount() {
      return errorCount;
    }

    @Override
    public long responseBytes() {
      return responseBytes;
    }

    @Override
    public long totalMicros() {
      return totalMicros;
    }

    @Override
    public long maxMicros() {
      return maxMicros;
    }

    @Override
    public long avgMicros() {
      return totalCount == 0 ? 0 : totalMicros / totalCount;
    }
  }

  @SuppressWarnings("unchecked")
  BodyContent readErrorContent(boolean responseAsBytes, HttpResponse<?> httpResponse) {
    if (responseAsBytes) {
      return readContent((HttpResponse<byte[]>) httpResponse);
    }
    final String contentType = contentType(httpResponse);
    final Object body = httpResponse.body();
    if (body instanceof String) {
      return BodyContent.of(contentType, (String) body);
    }
    if (body instanceof Stream) {
      var sb = new StringBuilder(50);
      for (Object line : ((Stream<Object>) body).collect(Collectors.toList())) {
        sb.append(line);
      }
      return BodyContent.of(contentType, sb.toString());
    }
    final String type = (body == null) ? "null" : body.getClass().toString();
    throw new IllegalStateException("Unable to translate response body to bytes? Maybe use HttpResponse directly instead?  Response body type: " + type);
  }

  @Override
  public BodyContent readContent(HttpResponse<byte[]> httpResponse) {
    final byte[] body = httpResponse.body();
    if (body != null && body.length > 0) {
      metricResBytes.add(body.length);
    }
    final byte[] bodyBytes = decodeContent(httpResponse);
    final String contentType = contentType(httpResponse);
    return BodyContent.of(contentType, bodyBytes);
  }

  String contentType(HttpResponse<?> httpResponse) {
    return firstHeader(httpResponse.headers(), "Content-Type", "content-type");
  }

  String contentEncoding(HttpResponse<?> httpResponse) {
    return firstHeader(httpResponse.headers(), "Content-Encoding", "content-encoding");
  }

  @Override
  public byte[] decodeContent(String encoding, byte[] body) {
    if ("gzip".equals(encoding)) {
      return GzipUtil.gzipDecode(body);
    }
    // todo: register decoders with context and use them
    return body;
  }

  @Override
  public byte[] decodeContent(HttpResponse<byte[]> httpResponse) {
    final String encoding = contentEncoding(httpResponse);
    return encoding == null ? httpResponse.body() : decodeContent(encoding, httpResponse.body());
  }

  String firstHeader(HttpHeaders headers, String... names) {
    final Map<String, List<String>> map = headers.map();
    for (final String key : names) {
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
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new HttpException(499, e);
    } catch (final Exception e) {
      throw new HttpException(499, e);
    }
  }

  <T> CompletableFuture<HttpResponse<T>> sendAsync(
      HttpRequest.Builder requestBuilder, HttpResponse.BodyHandler<T> bodyHandler) {
    return httpClient
        .sendAsync(requestBuilder.build(), bodyHandler)
        .handle(
            (r, e) -> {
              if (e != null) {
                if (e.getCause() instanceof InterruptedException) {
                  Thread.currentThread().interrupt();
                }
                throw new HttpException(499, e.getCause());
              }
              return r;
            });
  }

  <T> BodyContent write(T bean, Class<?> type, String contentType) {
    return bodyAdapter.beanWriter(type).write(bean, contentType);
  }

  <T> BodyContent write(T bean, Type type, String contentType) {
    return bodyAdapter.beanWriter(type).write(bean, contentType);
  }

  <T> BodyReader<T> beanReader(Class<T> type) {
    return bodyAdapter.beanReader(type);
  }

  <T> BodyReader<T> beanReader(Type type) {
    return bodyAdapter.beanReader(type);
  }

  <T> T readBean(Class<T> type, BodyContent content) {
    if (content.isEmpty()) {
      return null;
    }
    return bodyAdapter.beanReader(type).read(content);
  }

  <T> List<T> readList(Class<T> type, BodyContent content) {
    if (content.isEmpty()) {
      return Collections.emptyList();
    }
    return bodyAdapter.listReader(type).read(content);
  }

  @SuppressWarnings("unchecked")
  <T> T readBean(Type type, BodyContent content) {
    if (content.isEmpty()) {
      return null;
    }
    return (T) bodyAdapter.beanReader(type).read(content);
  }

  @SuppressWarnings("unchecked")
  <T> List<T> readList(Type type, BodyContent content) {
    if (content.isEmpty()) {
      return Collections.emptyList();
    }
    return (List<T>) bodyAdapter.listReader(type).read(content);
  }

  void afterResponse(DHttpClientRequest request) {
    metricResTotal.add(1);
    metricResMicros.add(request.responseTimeMicros());
    metricResMaxMicros.accumulate(request.responseTimeMicros());
    if (request.response().statusCode() >= 300) {
      metricResError.add(1);
    }
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
    final AuthToken authToken = tokenRef.get();
    if (authToken == null) {
      return obtainNewAuthToken();
    }
    final Duration expiration = authToken.expiration();
    if (expiration.isNegative()) {
      return obtainNewAuthToken();
    }
    if (backgroundRefreshDuration != null && expiration.compareTo(backgroundRefreshDuration) < 0) {
      backgroundTokenRequest();
    }
    return authToken.token();
  }

  private String obtainNewAuthToken() {
    final AuthToken authToken = authTokenProvider.obtainToken(request().skipAuthToken());
    tokenRef.set(authToken);
    return authToken.token();
  }

  private void backgroundTokenRequest() {
    final Instant lease = backgroundRefreshLease.get();
    if (lease != null && Instant.now().isBefore(lease)) {
      // a refresh is already in progress
      return;
    }
    // other requests should not trigger a refresh for the next 10 seconds
    backgroundRefreshLease.set(Instant.now().plusMillis(10_000));
    BGInvoke.invoke(this::backgroundNewTokenTask);
  }

  private void backgroundNewTokenTask() {
    try {
      obtainNewAuthToken();
    } catch (Exception e) {
      log.log(WARNING, "Error refreshing AuthToken in background", e);
    }
  }

  String maxResponseBody(String body) {
    return body.length() > 1_000 ? body.substring(0, 1_000) + " <truncated> ..." : body;
  }

  @Override
  public void close() {
    this.closed = true;
    if (Integer.getInteger("java.specification.version") >= 21) {
      try {
        MethodHandles.lookup()
            .findVirtual(java.net.http.HttpClient.class, "close", MethodType.methodType(void.class))
            .invokeExact(httpClient);
      } catch (Throwable t) {
        throw new IllegalStateException("Failed to close java.net.http.HttpClient instance");
      }
    }
  }
}
