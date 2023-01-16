package io.avaje.http.client;

import io.avaje.inject.BeanScope;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * The HTTP client context that we use to build and process requests.
 *
 * <pre>{@code
 *
 *   HttpClient client = HttpClient.builder()
 *       .baseUrl("http://localhost:8080")
 *       .bodyAdapter(new JacksonBodyAdapter())
 *       .build();
 *
 *  HelloDto dto = client.request()
 *       .path("hello")
 *       .queryParam("name", "Rob")
 *       .queryParam("say", "Whats up")
 *       .GET()
 *       .bean(HelloDto.class);
 *
 * }</pre>
 */
public interface HttpClient {

  /**
   * Return the builder to config and build the client context.
   *
   * <pre>{@code
   *
   *   HttpClient client = HttpClient.builder()
   *       .baseUrl("http://localhost:8080")
   *       .bodyAdapter(new JacksonBodyAdapter())
   *       .build();
   *
   *  HttpResponse<String> res = client.request()
   *       .path("hello")
   *       .GET().asString();
   *
   * }</pre>
   */
  static Builder builder() {
    return new DHttpClientBuilder();
  }

  /**
   * Return the http client API implementation.
   *
   * @param clientInterface A <code>@Client</code> interface with annotated API methods.
   * @param <T>             The service type.
   * @return The http client API implementation.
   */
  <T> T create(Class<T> clientInterface);

  /**
   * Create a new request.
   */
  HttpClientRequest request();

  /**
   * Return the body adapter used by the client context.
   * <p>
   * This is the body adapter used to convert request and response
   * bodies to java types. For example using Jackson with JSON payloads.
   */
  BodyAdapter converters();

  /**
   * Return the current aggregate metrics.
   * <p>
   * These metrics are collected for all requests sent via this context.
   */
  HttpClientContext.Metrics metrics();

  /**
   * Return the current metrics with the option of resetting the underlying counters.
   * <p>
   * These metrics are collected for all requests sent via this context.
   */
  HttpClientContext.Metrics metrics(boolean reset);

  /**
   * Builds the HttpClient.
   *
   * <pre>{@code
   *
   *   HttpClient client = HttpClient.builder()
   *       .baseUrl("http://localhost:8080")
   *       .bodyAdapter(new JacksonBodyAdapter())
   *       .build();
   *
   *  HelloDto dto = client.request()
   *       .path("hello")
   *       .queryParam("name", "Rob")
   *       .queryParam("say", "Whats up")
   *       .GET()
   *       .bean(HelloDto.class);
   *
   * }</pre>
   */
  interface Builder {

    /**
     * Set the base URL to use for requests created from the context.
     * <p>
     * Note that the base url can be replaced via {@link HttpClientRequest#url(String)}.
     */
    Builder baseUrl(String baseUrl);

    /**
     * Set the default request timeout.
     *
     * @see java.net.http.HttpRequest.Builder#timeout(Duration)
     */
    Builder requestTimeout(Duration requestTimeout);

    /**
     * Set the body adapter to use to convert beans to body content
     * and response content back to beans.
     */
    Builder bodyAdapter(BodyAdapter adapter);

    /**
     * Set a RetryHandler to use to retry requests.
     */
    Builder retryHandler(RetryHandler retryHandler);

    /**
     * Disable or enable built in request and response logging.
     * <p>
     * By default request logging is enabled. Set this to false to stop
     * the default {@link RequestLogger} being registered to log request
     * and response headers and bodies etc.
     * <p>
     * With logging level set to {@code DEBUG} for
     * {@code io.avaje.http.client.RequestLogger} the request and response
     * are logged as a summary with response status and time.
     * <p>
     * Set the logging level to {@code TRACE} to include the request
     * and response headers and body payloads with truncation for large
     * bodies.
     *
     * <h3>Suppression</h3>
     * <p>
     * We can also use {@link HttpClientRequest#suppressLogging()} to suppress
     * logging on specific requests.
     * <p>
     * Logging of Authorization headers is suppressed.
     * {@link AuthTokenProvider} requests are suppressed.
     *
     * @param requestLogging Disable/enable the registration of the default logger
     * @see RequestLogger
     */
    Builder requestLogging(boolean requestLogging);

    /**
     * Add a request listener. Multiple listeners may be added, when
     * do so they will process events in the order they were added.
     * <p>
     * Note that {@link RequestLogger} is an implementation for debug
     * logging request/response headers and content which is registered
     * by default depending on {@link #requestLogging(boolean)}.
     *
     * @see RequestLogger
     */
    Builder requestListener(RequestListener requestListener);

    /**
     * Add a request interceptor. Multiple interceptors may be added.
     */
    Builder requestIntercept(RequestIntercept requestIntercept);

    /**
     * Add a Authorization token provider.
     * <p>
     * When set all requests are expected to use a Authorization Bearer token
     * unless they are marked via {@link HttpClientRequest#skipAuthToken()}.
     * <p>
     * The AuthTokenProvider obtains a new token typically with an expiry. This
     * is automatically called as needed and the Authorization Bearer header set
     * on all requests (not marked with skipAuthToken()).
     */
    Builder authTokenProvider(AuthTokenProvider authTokenProvider);

    /**
     * Set the underlying HttpClient to use.
     * <p>
     * Used when we wish to control all options of the HttpClient.
     */
    Builder client(java.net.http.HttpClient client);

    /**
     * Specify a cookie handler to use on the HttpClient. This would override the default cookie handler.
     *
     * @see java.net.http.HttpClient.Builder#cookieHandler(CookieHandler)
     */
    Builder cookieHandler(CookieHandler cookieHandler);

    /**
     * Specify the redirect policy. Defaults to HttpClient.Redirect.NORMAL.
     *
     * @see java.net.http.HttpClient.Builder#followRedirects(java.net.http.HttpClient.Redirect)
     */
    Builder redirect(java.net.http.HttpClient.Redirect redirect);

    /**
     * Specify the HTTP version. Defaults to not set.
     *
     * @see java.net.http.HttpClient.Builder#version(java.net.http.HttpClient.Version)
     */
    Builder version(java.net.http.HttpClient.Version version);

    /**
     * Specify the Executor to use for asynchronous tasks.
     * If not specified a default executor will be used.
     *
     * @see java.net.http.HttpClient.Builder#executor(Executor)
     */
    Builder executor(Executor executor);

    /**
     * Set the proxy to the underlying {@link java.net.http.HttpClient}.
     *
     * @see java.net.http.HttpClient.Builder#proxy(ProxySelector)
     */
    Builder proxy(ProxySelector proxySelector);

    /**
     * Set the sslContext to the underlying {@link java.net.http.HttpClient}.
     *
     * @see java.net.http.HttpClient.Builder#sslContext(SSLContext)
     */
    Builder sslContext(SSLContext sslContext);

    /**
     * Set the sslParameters to the underlying {@link java.net.http.HttpClient}.
     *
     * @see java.net.http.HttpClient.Builder#sslParameters(SSLParameters)
     */
    Builder sslParameters(SSLParameters sslParameters);

    /**
     * Set a HttpClient authenticator to the underlying {@link java.net.http.HttpClient}.
     *
     * @see java.net.http.HttpClient.Builder#authenticator(Authenticator)
     */
    Builder authenticator(Authenticator authenticator);

    /**
     * Set the priority for HTTP/2 requests to the underlying {@link java.net.http.HttpClient}.
     *
     * @see java.net.http.HttpClient.Builder#priority(int)
     */
    Builder priority(int priority);

    /**
     * Configure BodyAdapter and RetryHandler using dependency injection BeanScope.
     */
    Builder configureWith(BeanScope beanScope);

    /**
     * Return the state of the builder.
     */
    Builder.State state();

    /**
     * Build and return the context.
     *
     * <pre>{@code
     *
     *   HttpClient client = HttpClient.builder()
     *       .baseUrl("http://localhost:8080")
     *       .bodyAdapter(new JacksonBodyAdapter())
     *       .build();
     *
     *  HelloDto dto = client.request()
     *       .path("hello")
     *       .queryParam("say", "Whats up")
     *       .GET()
     *       .bean(HelloDto.class);
     *
     * }</pre>
     */
    HttpClient build();

    /**
     * The state of the builder with methods to read the set state.
     */
    interface State {

      /**
       * Return the base URL.
       */
      String baseUrl();

      /**
       * Return the body adapter.
       */
      BodyAdapter bodyAdapter();

      /**
       * Return the HttpClient.
       */
      java.net.http.HttpClient client();

      /**
       * Return true if requestLogging is on.
       */
      boolean requestLogging();

      /**
       * Return the request timeout.
       */
      Duration requestTimeout();

      /**
       * Return the retry handler.
       */
      RetryHandler retryHandler();
    }
  }

  /**
   * Statistic metrics collected to provide an overview of activity of this client.
   */
  interface Metrics {

    /**
     * Return the total number of responses.
     */
    long totalCount();

    /**
     * Return the total number of error responses (status code >= 300).
     */
    long errorCount();

    /**
     * Return the total response bytes (excludes streaming responses).
     */
    long responseBytes();

    /**
     * Return the total response time in microseconds.
     */
    long totalMicros();

    /**
     * Return the max response time in microseconds (since the last reset).
     */
    long maxMicros();

    /**
     * Return the average response time in microseconds.
     */
    long avgMicros();
  }
}
