package io.avaje.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.inject.BeanScope;
import io.avaje.jsonb.Jsonb;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.ProxySelector;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

final class DHttpClientBuilder implements HttpClient.Builder, HttpClient.Builder.State {

  private java.net.http.HttpClient client;
  private String baseUrl;
  private boolean requestLogging = true;
  private Duration connectionTimeout = Duration.ofSeconds(20);
  private Duration requestTimeout = Duration.ofSeconds(20);
  private BodyAdapter bodyAdapter;
  private RetryHandler retryHandler;
  private Function<HttpException, RuntimeException> errorHandler;
  private AuthTokenProvider authTokenProvider;

  private CookieHandler cookieHandler = new CookieManager();
  private java.net.http.HttpClient.Redirect redirect = java.net.http.HttpClient.Redirect.NORMAL;
  private java.net.http.HttpClient.Version version;
  private Executor executor;
  private ProxySelector proxy;
  private SSLContext sslContext;
  private SSLParameters sslParameters;
  private Authenticator authenticator;
  private int priority;

  private final List<RequestIntercept> interceptors = new ArrayList<>();
  private final List<RequestListener> listeners = new ArrayList<>();

  private void configureRetryHandler(BeanScope beanScope) {
    beanScope.getOptional(RetryHandler.class)
      .ifPresent(this::setRetryHandler);
  }

  private void setRetryHandler(RetryHandler retryHandler) {
    this.retryHandler = retryHandler;
  }

  private void configureBodyAdapter(BeanScope beanScope) {
    Optional<BodyAdapter> body = beanScope.getOptional(BodyAdapter.class);
    if (body.isPresent()) {
      bodyAdapter = body.get();
    } else if (beanScope.contains("io.avaje.jsonb.Jsonb")) {
      bodyAdapter = new JsonbBodyAdapter(beanScope.get(Jsonb.class));
    } else if (beanScope.contains("com.fasterxml.jackson.databind.ObjectMapper")) {
      ObjectMapper objectMapper = beanScope.get(ObjectMapper.class);
      bodyAdapter = new JacksonBodyAdapter(objectMapper);
    }
  }

  private RequestListener buildListener() {
    if (listeners.isEmpty()) {
      return null;
    } else if (listeners.size() == 1) {
      return listeners.get(0);
    } else {
      return new DRequestListeners(listeners);
    }
  }

  private RequestIntercept buildIntercept() {
    if (interceptors.isEmpty()) {
      return null;
    } else if (interceptors.size() == 1) {
      return interceptors.get(0);
    } else {
      return new DRequestInterceptors(interceptors);
    }
  }

  private java.net.http.HttpClient defaultClient() {
    final var builder = java.net.http.HttpClient.newBuilder()
      .followRedirects(redirect)
      .connectTimeout(connectionTimeout);

    if (cookieHandler != null) {
      builder.cookieHandler(cookieHandler);
    }
    if (version != null) {
      builder.version(version);
    }
    if (executor != null) {
      builder.executor(executor);
    }
    if (proxy != null) {
      builder.proxy(proxy);
    }
    if (sslContext != null) {
      builder.sslContext(sslContext);
    }
    if (sslParameters != null) {
      builder.sslParameters(sslParameters);
    }
    if (authenticator != null) {
      builder.authenticator(authenticator);
    }
    if (priority > 0) {
      builder.priority(priority);
    }
    return builder.build();
  }

  /**
   * Create a reasonable default BodyAdapter if avaje-jsonb or Jackson are present.
   */
  private BodyAdapter defaultBodyAdapter() {
    if (detectJsonb()) {
      bodyAdapter = new JsonbBodyAdapter();
    } else if (detectJackson()) {
      bodyAdapter = new JacksonBodyAdapter();
    }
    return bodyAdapter;
  }

  private boolean detectJsonb() {
    return detectTypeExists("io.avaje.jsonb.Jsonb");
  }

  private boolean detectJackson() {
    return detectTypeExists("com.fasterxml.jackson.databind.ObjectMapper");
  }

  private boolean detectTypeExists(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException | IllegalAccessError e) {
      return false;
    }
  }

  private DHttpClientContext buildClient() {
    requireNonNull(baseUrl, "baseUrl is not specified");
    requireNonNull(requestTimeout, "requestTimeout is not specified");
    final var httpClient = client != null ? client : defaultClient();
    if (requestLogging) {
      // register the built-in request/response logging
      this.listeners.add(new RequestLogger());
    }
    if (bodyAdapter == null) {
      bodyAdapter = defaultBodyAdapter();
    }
    return new DHttpClientContext(
      httpClient,
      baseUrl,
      requestTimeout,
      bodyAdapter,
      retryHandler,
      errorHandler,
      buildListener(),
      authTokenProvider,
      buildIntercept());
  }

  DHttpClientBuilder() {
  }

  @Override
  public HttpClient.Builder client(java.net.http.HttpClient client) {
    this.client = client;
    return this;
  }

  @Override
  public HttpClient.Builder baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  @Override
  public HttpClient.Builder connectionTimeout(Duration connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
    return this;
  }

  @Override
  public HttpClient.Builder requestTimeout(Duration requestTimeout) {
    this.requestTimeout = requestTimeout;
    return this;
  }

  @Override
  public HttpClient.Builder bodyAdapter(BodyAdapter adapter) {
    this.bodyAdapter = adapter;
    return this;
  }

  @Override
  public HttpClient.Builder retryHandler(RetryHandler retryHandler) {
    this.retryHandler = retryHandler;
    return this;
  }

  @Override
  public HttpClient.Builder globalErrorMapper(Function<HttpException, RuntimeException> handler) {
    this.errorHandler = handler;
    return this;
  }

  @Override
  public HttpClient.Builder requestLogging(boolean requestLogging) {
    this.requestLogging = requestLogging;
    return this;
  }

  @Override
  public HttpClient.Builder requestListener(RequestListener... requestListener) {
    Collections.addAll(listeners, requestListener);
    return this;
  }

  @Override
  public HttpClient.Builder requestIntercept(RequestIntercept... requestIntercept) {
    Collections.addAll(interceptors, requestIntercept);
    return this;
  }

  @Override
  public HttpClient.Builder authTokenProvider(AuthTokenProvider authTokenProvider) {
    this.authTokenProvider = authTokenProvider;
    return this;
  }

  @Override
  public HttpClient.Builder cookieHandler(CookieHandler cookieHandler) {
    this.cookieHandler = cookieHandler;
    return this;
  }

  @Override
  public HttpClient.Builder redirect(java.net.http.HttpClient.Redirect redirect) {
    this.redirect = redirect;
    return this;
  }

  @Override
  public HttpClient.Builder version(java.net.http.HttpClient.Version version) {
    this.version = version;
    return this;
  }

  @Override
  public HttpClient.Builder executor(Executor executor) {
    this.executor = executor;
    return this;
  }

  @Override
  public HttpClient.Builder proxy(ProxySelector proxySelector) {
    this.proxy = proxySelector;
    return this;
  }

  @Override
  public HttpClient.Builder sslContext(SSLContext sslContext) {
    this.sslContext = sslContext;
    return this;
  }

  @Override
  public HttpClient.Builder sslParameters(SSLParameters sslParameters) {
    this.sslParameters = sslParameters;
    return this;
  }

  @Override
  public HttpClient.Builder authenticator(Authenticator authenticator) {
    this.authenticator = authenticator;
    return this;
  }

  @Override
  public HttpClient.Builder priority(int priority) {
    this.priority = priority;
    return this;
  }

  @Override
  public HttpClient.Builder.State state() {
    return this;
  }

  @Override
  public HttpClient.Builder configureWith(BeanScope beanScope) {
    if (bodyAdapter == null) {
      configureBodyAdapter(beanScope);
    }
    if (retryHandler == null) {
      configureRetryHandler(beanScope);
    }
    return this;
  }

  @Override
  public HttpClient build() {
    return buildClient();
  }

  @Override
  public String baseUrl() {
    return baseUrl;
  }

  @Override
  public BodyAdapter bodyAdapter() {
    return bodyAdapter;
  }

  @Override
  public java.net.http.HttpClient client() {
    return client;
  }

  @Override
  public boolean requestLogging() {
    return requestLogging;
  }

  @Override
  public Duration requestTimeout() {
    return requestTimeout;
  }

  @Override
  public RetryHandler retryHandler() {
    return retryHandler;
  }

}
