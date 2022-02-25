package io.avaje.http.client;

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
import java.util.concurrent.Executor;

import static java.util.Objects.requireNonNull;

final class DHttpClientContextBuilder implements HttpClientContext.Builder {

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
  public HttpClientContext build() {
    requireNonNull(baseUrl, "baseUrl is not specified");
    requireNonNull(requestTimeout, "requestTimeout is not specified");
    if (client == null) {
      client = defaultClient();
    }
    if (requestLogging) {
      // register the built in request/response logging
      requestListener(new RequestLogger());
    }
    return new DHttpClientContext(client, baseUrl, requestTimeout, bodyAdapter, retryHandler, buildListener(), authTokenProvider, buildIntercept());
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

}
