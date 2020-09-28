package io.avaje.http.client;

import java.net.CookieHandler;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * The HTTP client context that we use to build and process requests.
 */
public interface HttpClientContext {

  /**
   * Return the builder to config and build the client context.
   */
  static HttpClientContext.Builder newBuilder() {
    return new DHttpClientContextBuilder();
  }

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
   */
  interface Builder {

    /**
     * Set the underlying HttpClient to use.
     */
    Builder with(HttpClient client);

    /**
     * Set the base URL to use for requests created from the context.
     */
    Builder withBaseUrl(String baseUrl);

    /**
     * Set the default request timeout.
     */
    Builder withRequestTimeout(Duration requestTimeout);

    /**
     * Set the body adapter to use to convert beans to body content
     * and response content back to beans.
     */
    Builder withBodyAdapter(BodyAdapter adapter);

    /**
     * Add a response listener. Note that {@link RequestLogger} is an
     * implementation for debug logging request/response headers and
     * content.
     */
    Builder withResponseListener(ResponseListener requestListener);

    /**
     * Specify a cookie handler to use on the HttpClient. This would override the default cookie handler.
     */
    Builder withCookieHandler(CookieHandler cookieHandler);

    /**
     * Specify the redirect policy. Defaults to HttpClient.Redirect.NORMAL.
     */
    Builder withRedirect(HttpClient.Redirect redirect);

    /**
     * Build and return the context.
     */
    HttpClientContext build();
  }
}
