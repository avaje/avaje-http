package io.avaje.http.client;

import io.avaje.inject.BeanScope;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.time.Duration;
import java.util.concurrent.Executor;

final class DHttpClientBuilder extends DBaseBuilder implements HttpClient.Builder, HttpClient.Builder.State {

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
  public HttpClient.Builder requestLogging(boolean requestLogging) {
    this.requestLogging = requestLogging;
    return this;
  }

  @Override
  public HttpClient.Builder requestListener(RequestListener requestListener) {
    this.listeners.add(requestListener);
    return this;
  }

  @Override
  public HttpClient.Builder requestIntercept(RequestIntercept requestIntercept) {
    this.interceptors.add(requestIntercept);
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
    super.configureFromScope(beanScope);
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
