package io.avaje.http.client;

import java.net.CookieHandler;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * The HTTP client context that we use to build and process requests.
 *
 * <pre>{@code
 *
 *   HttpClientContext ctx = HttpClientContext.newBuilder()
 *       .withBaseUrl("http://localhost:8080")
 *       .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
 *       .build();
 *
 *  HelloDto dto = ctx.request()
 *       .path("hello")
 *       .queryParam("name", "Rob")
 *       .queryParam("say", "Ki ora")
 *       .get()
 *       .bean(HelloDto.class);
 *
 * }</pre>
 */
public interface HttpClientContext {

  /**
   * Return the builder to config and build the client context.
   *
   * <pre>{@code
   *
   *   HttpClientContext ctx = HttpClientContext.newBuilder()
   *       .withBaseUrl("http://localhost:8080")
   *       .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
   *       .build();
   *
   *  HttpResponse<String> res = ctx.request()
   *       .path("hello")
   *       .get().asString();
   *
   * }</pre>
   */
  static HttpClientContext.Builder newBuilder() {
    return new DHttpClientContextBuilder();
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
   * Return a UrlBuilder to use to build an URL taking into
   * account the base URL.
   */
  UrlBuilder url();

  /**
   * Return the body adapter used by the client context.
   * <p>
   * This is the body adapter used to convert request and response
   * bodies to java types. For example using Jackson with JSON payloads.
   */
  BodyAdapter converters();

  /**
   * Return the underlying http client.
   */
  HttpClient httpClient();

  /**
   * Check the response status code and throw HttpException if the status
   * code is in the error range.
   */
  void checkResponse(HttpResponse<?> response);

  /**
   * Return the response content taking into account content encoding.
   *
   * @param httpResponse The HTTP response to decode the content from
   * @return The decoded content
   */
  BodyContent readContent(HttpResponse<byte[]> httpResponse);

  /**
   * Decode the response content given the <code>Content-Encoding</code> http header.
   *
   * @param httpResponse The HTTP response
   * @return The decoded content
   */
  byte[] decodeContent(HttpResponse<byte[]> httpResponse);

  /**
   * Decode the body using the given encoding.
   *
   * @param encoding The encoding used to decode the content
   * @param content  The raw content being decoded
   * @return The decoded content
   */
  byte[] decodeContent(String encoding, byte[] content);

  /**
   * Builds the HttpClientContext.
   *
   * <pre>{@code
   *
   *   HttpClientContext ctx = HttpClientContext.newBuilder()
   *       .withBaseUrl("http://localhost:8080")
   *       .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
   *       .build();
   *
   *  HelloDto dto = ctx.request()
   *       .path("hello")
   *       .queryParam("name", "Rob")
   *       .queryParam("say", "Ki ora")
   *       .get()
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
    Builder with(HttpClient client);

    /**
     * Set the base URL to use for requests created from the context.
     * <p>
     * Note that the base url can be replaced via {@link HttpClientRequest#url(String)}.
     */
    Builder withBaseUrl(String baseUrl);

    /**
     * Set the default request timeout.
     *
     * @see java.net.http.HttpRequest.Builder#timeout(Duration)
     */
    Builder withRequestTimeout(Duration requestTimeout);

    /**
     * Set the body adapter to use to convert beans to body content
     * and response content back to beans.
     */
    Builder withBodyAdapter(BodyAdapter adapter);

    /**
     * Add a request listener. Note that {@link RequestLogger} is an
     * implementation for debug logging request/response headers and
     * content.
     */
    Builder withRequestListener(RequestListener requestListener);

    /**
     * Add a request interceptor.
     */
    Builder withRequestIntercept(RequestIntercept requestIntercept);

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
    Builder withAuthTokenProvider(AuthTokenProvider authTokenProvider);

    /**
     * Specify a cookie handler to use on the HttpClient. This would override the default cookie handler.
     *
     * @see HttpClient.Builder#cookieHandler(CookieHandler)
     */
    Builder withCookieHandler(CookieHandler cookieHandler);

    /**
     * Specify the redirect policy. Defaults to HttpClient.Redirect.NORMAL.
     *
     * @see HttpClient.Builder#followRedirects(HttpClient.Redirect)
     */
    Builder withRedirect(HttpClient.Redirect redirect);

    /**
     * Specify the HTTP version. Defaults to not set.
     *
     * @see HttpClient.Builder#version(HttpClient.Version)
     */
    Builder withVersion(HttpClient.Version version);

    /**
     * Specify the Executor to use for asynchronous tasks.
     * If not specified a default executor will be used.
     *
     * @see HttpClient.Builder#executor(Executor)
     */
    Builder withExecutor(Executor executor);

    /**
     * Build and return the context.
     *
     * <pre>{@code
     *
     *   HttpClientContext ctx = HttpClientContext.newBuilder()
     *       .withBaseUrl("http://localhost:8080")
     *       .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
     *       .build();
     *
     *  HelloDto dto = ctx.request()
     *       .path("hello")
     *       .queryParam("say", "Ki ora")
     *       .get()
     *       .bean(HelloDto.class);
     *
     * }</pre>
     */
    HttpClientContext build();
  }
}
