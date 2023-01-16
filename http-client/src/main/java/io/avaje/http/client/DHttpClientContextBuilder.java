package io.avaje.http.client;

import io.avaje.inject.BeanScope;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executor;

final class DHttpClientContextBuilder extends DBaseBuilder implements HttpClientContext.Builder, HttpClientContext.Builder.State {

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
  public HttpClientContext.Builder connectionTimeout(Duration connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
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
    super.configureFromScope(beanScope);
    return this;
  }

  @Override
  public HttpClientContext build() {
    return super.buildClient();
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

}
