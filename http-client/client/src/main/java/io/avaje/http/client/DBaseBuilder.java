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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static java.util.Objects.requireNonNull;

abstract class DBaseBuilder {

  java.net.http.HttpClient client;
  String baseUrl;
  boolean requestLogging = true;
  Duration requestTimeout = Duration.ofSeconds(20);
  BodyAdapter bodyAdapter;
  RetryHandler retryHandler;
  AuthTokenProvider authTokenProvider;

  CookieHandler cookieHandler = new CookieManager();
  java.net.http.HttpClient.Redirect redirect = java.net.http.HttpClient.Redirect.NORMAL;
  java.net.http.HttpClient.Version version;
  Executor executor;
  ProxySelector proxy;
  SSLContext sslContext;
  SSLParameters sslParameters;
  Authenticator authenticator;
  int priority;

  final List<RequestIntercept> interceptors = new ArrayList<>();
  final List<RequestListener> listeners = new ArrayList<>();

  void configureFromScope(BeanScope beanScope) {
    if (bodyAdapter == null) {
      configureBodyAdapter(beanScope);
    }
    if (retryHandler == null) {
      configureRetryHandler(beanScope);
    }
  }

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
    final java.net.http.HttpClient.Builder builder = java.net.http.HttpClient.newBuilder()
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
  private BodyAdapter defaultBodyAdapter() {
    try {
      return detectJsonb() ? new JsonbBodyAdapter()
        : detectJackson() ? new JacksonBodyAdapter()
        : null;
    } catch (IllegalAccessError e) {
      // not in module path
      return null;
    }
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
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  DHttpClientContext buildClient() {
    requireNonNull(baseUrl, "baseUrl is not specified");
    requireNonNull(requestTimeout, "requestTimeout is not specified");
    if (client == null) {
      client = defaultClient();
    }
    if (requestLogging) {
      // register the builtin request/response logging
      this.listeners.add(new RequestLogger());
    }
    if (bodyAdapter == null) {
      bodyAdapter = defaultBodyAdapter();
    }
    return new DHttpClientContext(client, baseUrl, requestTimeout, bodyAdapter, retryHandler, buildListener(), authTokenProvider, buildIntercept());
  }

}
