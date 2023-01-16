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
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static java.util.Objects.requireNonNull;

final class DHttpClientContextBuilder implements HttpClientContext.Builder, HttpClientContext.Builder.State {

  private HttpClient client;
  private String baseUrl;
  private boolean requestLogging = true;
  private Duration requestTimeout = Duration.ofSeconds(20);
  private BodyAdapter bodyAdapter;
  private RetryHandler retryHandler;
  private AuthTokenProvider authTokenProvider;

  private CookieHandler cookieHandler = new CookieManager();
  private HttpClient.Redirect redirect = HttpClient.Redirect.NORMAL;
  private HttpClient.Version version;
  private Executor executor;
  private ProxySelector proxy;
  private SSLContext sslContext;
  private SSLParameters sslParameters;
  private Authenticator authenticator;
  private int priority;

  private final List<RequestIntercept> interceptors = new ArrayList<>();
  private final List<RequestListener> listeners = new ArrayList<>();

  DHttpClientContextBuilder() {
  }

  @Override
  public HttpClientContext.Builder client(HttpClient client) {
    this.client = client;
    return this;
  }

  @Override
  public HttpClientContext.Builder baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  @Override
  public HttpClientContext.Builder requestTimeout(Duration requestTimeout) {
    this.requestTimeout = requestTimeout;
    return this;
  }

  @Override
  public HttpClientContext.Builder bodyAdapter(BodyAdapter adapter) {
    this.bodyAdapter = adapter;
    return this;
  }

  @Override
  public HttpClientContext.Builder retryHandler(RetryHandler retryHandler) {
    this.retryHandler = retryHandler;
    return this;
  }

  @Override
  public HttpClientContext.Builder requestLogging(boolean requestLogging) {
    this.requestLogging = requestLogging;
    return this;
  }

  @Override
  public HttpClientContext.Builder requestListener(RequestListener requestListener) {
    this.listeners.add(requestListener);
    return this;
  }

  @Override
  public HttpClientContext.Builder requestIntercept(RequestIntercept requestIntercept) {
    this.interceptors.add(requestIntercept);
    return this;
  }

  @Override
  public HttpClientContext.Builder authTokenProvider(AuthTokenProvider authTokenProvider) {
    this.authTokenProvider = authTokenProvider;
    return this;
  }

  @Override
  public HttpClientContext.Builder cookieHandler(CookieHandler cookieHandler) {
    this.cookieHandler = cookieHandler;
    return this;
  }

  @Override
  public HttpClientContext.Builder redirect(HttpClient.Redirect redirect) {
    this.redirect = redirect;
    return this;
  }

  @Override
  public HttpClientContext.Builder version(HttpClient.Version version) {
    this.version = version;
    return this;
  }

  @Override
  public HttpClientContext.Builder executor(Executor executor) {
    this.executor = executor;
    return this;
  }

  @Override
  public HttpClientContext.Builder proxy(ProxySelector proxySelector) {
    this.proxy = proxySelector;
    return this;
  }

  @Override
  public HttpClientContext.Builder sslContext(SSLContext sslContext) {
    this.sslContext = sslContext;
    return this;
  }

  @Override
  public HttpClientContext.Builder sslParameters(SSLParameters sslParameters) {
    this.sslParameters = sslParameters;
    return this;
  }

  @Override
  public HttpClientContext.Builder authenticator(Authenticator authenticator) {
    this.authenticator = authenticator;
    return this;
  }

  @Override
  public HttpClientContext.Builder priority(int priority) {
    this.priority = priority;
    return this;
  }

  @Override
  public State state() {
    return this;
  }

  @Override
  public HttpClientContext.Builder configureWith(BeanScope beanScope) {
    if (bodyAdapter == null) {
      configureBodyAdapter(beanScope);
    }
    if (retryHandler == null) {
      configureRetryHandler(beanScope);
    }
    return this;
  }

  private void configureRetryHandler(BeanScope beanScope) {
    beanScope.getOptional(RetryHandler.class)
      .ifPresent(this::retryHandler);
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

  @Override
  public HttpClientContext build() {
    requireNonNull(baseUrl, "baseUrl is not specified");
    requireNonNull(requestTimeout, "requestTimeout is not specified");
    if (client == null) {
      client = defaultClient();
    }
    if (requestLogging) {
      // register the builtin request/response logging
      requestListener(new RequestLogger());
    }
    if (bodyAdapter == null) {
      bodyAdapter = defaultBodyAdapter();
    }
    return new DHttpClientContext(client, baseUrl, requestTimeout, bodyAdapter, retryHandler, buildListener(), authTokenProvider, buildIntercept());
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
  public HttpClient client() {
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

  private HttpClient defaultClient() {
    final HttpClient.Builder builder = HttpClient.newBuilder()
      .followRedirects(redirect)
      .connectTimeout(Duration.ofSeconds(20));
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
  BodyAdapter defaultBodyAdapter() {
    try {
      return detectJsonb() ? new JsonbBodyAdapter()
        : detectJackson() ? new JacksonBodyAdapter()
        : null;
    } catch (IllegalAccessError e) {
      // not in module path
      return null;
    }
  }

  boolean detectJsonb() {
    return detectTypeExists("io.avaje.jsonb.Jsonb");
  }

  boolean detectJackson() {
    return detectTypeExists("com.fasterxml.jackson.databind.ObjectMapper");
  }

  private boolean detectTypeExists(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

}
