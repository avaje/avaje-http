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

/**
 * Deprecated in favor of {@link io.avaje.http.client.HttpClient}.
 * Migrate to using {@link io.avaje.http.client.HttpClient#builder()}.
 * <p>
 * The HTTP client context that we use to build and process requests.
 *
 * <pre>{@code
 *
 *   HttpClientContext ctx = HttpClientContext.builder()
 *       .baseUrl("http://localhost:8080")
 *       .bodyAdapter(new JacksonBodyAdapter())
 *       .build();
 *
 *  HelloDto dto = ctx.request()
 *       .path("hello")
 *       .queryParam("name", "Rob")
 *       .queryParam("say", "Whats up")
 *       .GET()
 *       .bean(HelloDto.class);
 *
 * }</pre>
 */
@Deprecated
public interface HttpClientContext extends io.avaje.http.client.HttpClient {

  /**
   * Deprecated - migrate to {@link io.avaje.http.client.HttpClient#builder()}.
   * <p>
   * Return the builder to config and build the client context.
   *
   * <pre>{@code
   *
   *   HttpClientContext ctx = HttpClientContext.builder()
   *       .baseUrl("http://localhost:8080")
   *       .bodyAdapter(new JacksonBodyAdapter())
   *       .build();
   *
   *  HttpResponse<String> res = ctx.request()
   *       .path("hello")
   *       .GET().asString();
   *
   * }</pre>
   */
  @Deprecated
  static HttpClientContext.Builder builder() {
    return new DHttpClientContextBuilder();
  }

  /**
   * Deprecated - migrate to builder().
   */
  @Deprecated
  static HttpClientContext.Builder newBuilder() {
    return builder();
  }

  /**
   * Builds the HttpClientContext.
   *
   * <pre>{@code
   *
   *   HttpClientContext ctx = HttpClientContext.builder()
   *       .baseUrl("http://localhost:8080")
   *       .bodyAdapter(new JacksonBodyAdapter())
   *       .build();
   *
   *  HelloDto dto = ctx.request()
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
     * Set the underlying HttpClient to use.
     * <p>
     * Used when we wish to control all options of the HttpClient.
     */
    Builder client(HttpClient client);

    /**
     * Set the base URL to use for requests created from the context.
     * <p>
     * Note that the base url can be replaced via {@link HttpClientRequest#url(String)}.
     */
    Builder baseUrl(String baseUrl);

    /**
     * Set the connection timeout to use.
     *
     * @see java.net.http.HttpClient.Builder#connectTimeout(Duration)
     */
    Builder connectionTimeout(Duration connectionTimeout);

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
     * Specify a cookie handler to use on the HttpClient. This would override the default cookie handler.
     *
     * @see HttpClient.Builder#cookieHandler(CookieHandler)
     */
    Builder cookieHandler(CookieHandler cookieHandler);

    /**
     * Specify the redirect policy. Defaults to HttpClient.Redirect.NORMAL.
     *
     * @see HttpClient.Builder#followRedirects(HttpClient.Redirect)
     */
    Builder redirect(HttpClient.Redirect redirect);

    /**
     * Specify the HTTP version. Defaults to not set.
     *
     * @see HttpClient.Builder#version(HttpClient.Version)
     */
    Builder version(HttpClient.Version version);

    /**
     * Specify the Executor to use for asynchronous tasks.
     * If not specified a default executor will be used.
     *
     * @see HttpClient.Builder#executor(Executor)
     */
    Builder executor(Executor executor);

    /**
     * Set the proxy to the underlying {@link HttpClient}.
     *
     * @see HttpClient.Builder#proxy(ProxySelector)
     */
    Builder proxy(ProxySelector proxySelector);

    /**
     * Set the sslContext to the underlying {@link HttpClient}.
     *
     * @see HttpClient.Builder#sslContext(SSLContext)
     */
    Builder sslContext(SSLContext sslContext);

    /**
     * Set the sslParameters to the underlying {@link HttpClient}.
     *
     * @see HttpClient.Builder#sslParameters(SSLParameters)
     */
    Builder sslParameters(SSLParameters sslParameters);

    /**
     * Set a HttpClient authenticator to the underlying {@link HttpClient}.
     *
     * @see HttpClient.Builder#authenticator(Authenticator)
     */
    Builder authenticator(Authenticator authenticator);

    /**
     * Set the priority for HTTP/2 requests to the underlying {@link HttpClient}.
     *
     * @see HttpClient.Builder#priority(int)
     */
    Builder priority(int priority);

    /**
     * Configure BodyAdapter and RetryHandler using dependency injection BeanScope.
     */
    Builder configureWith(BeanScope beanScope);

    /**
     * Return the state of the builder.
     */
    State state();

    /**
     * Build and return the context.
     *
     * <pre>{@code
     *
     *   HttpClientContext ctx = HttpClientContext.builder()
     *       .baseUrl("http://localhost:8080")
     *       .bodyAdapter(new JacksonBodyAdapter())
     *       .build();
     *
     *  HelloDto dto = ctx.request()
     *       .path("hello")
     *       .queryParam("say", "Whats up")
     *       .GET()
     *       .bean(HelloDto.class);
     *
     * }</pre>
     */
    HttpClientContext build();

    /**
     * The state of the builder with methods to read the set state.
     */
    interface State extends io.avaje.http.client.HttpClient.Builder.State {

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
