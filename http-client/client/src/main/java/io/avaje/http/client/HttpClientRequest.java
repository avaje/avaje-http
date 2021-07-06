package io.avaje.http.client;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Http request that is built and sent to the server.
 * <p>
 * Largely wraps the standard JDK HttpRequest with additional
 * support for converting beans to body content and converting
 * beans from response content.
 *
 * <pre>{@code
 *
 *  HelloDto dto = clientContext.request()
 *       .path("hello").queryParam("name", "Rob").queryParam("say", "Ki ora")
 *       .GET()
 *       .bean(HelloDto.class);
 *
 * }</pre>
 *
 * @see HttpClientContext
 */
public interface HttpClientRequest {

  /**
   * For this request skip using an Authorization token.
   * <p>
   * This is automatically set on the request passed to
   * {@link AuthTokenProvider#obtainToken(HttpClientRequest)}.
   */
  HttpClientRequest skipAuthToken();

  /**
   * For this request suppress payload logging.
   * <p>
   * The payload contains sensitive content and the request and response content
   * should be suppressed and not included in request logging.
   */
  HttpClientRequest suppressLogging();

  /**
   * Set the request timeout to use for this request. When not set the default
   * request timeout will be used.
   *
   * @param requestTimeout The request timeout to use for this request.
   * @return The request being built
   */
  HttpClientRequest requestTimeout(Duration requestTimeout);

  /**
   * Add the header to the request.
   *
   * @param name  The header name
   * @param value The header value
   * @return The request being built
   */
  HttpClientRequest header(String name, String value);

  /**
   * Add the header to the request implicitly converting the value to a String.
   *
   * @param name  The header name
   * @param value The header value
   * @return The request being built
   */
  HttpClientRequest header(String name, Object value);

  /**
   * Add the headers to the request via map.
   *
   * @param headers The headers as name value map to add
   * @return The request being built
   */
  HttpClientRequest header(Map<String, ?> headers);

  /**
   * Return the header values that have been set for the given header name.
   *
   * @return The headers values or an empty collection if the header has not been specified yet.
   */
  List<String> header(String name);

  /**
   * Set if body content should be gzip encoded.
   *
   * @param gzip Set true to gzip encode the body content.
   * @return The request being built
   */
  HttpClientRequest gzip(boolean gzip);

  /**
   * Set the URL to use replacing the base URL.
   * <pre>{code
   *
   *  HttpResponse<String> res = clientContext.request()
   *       .url("http://127.0.0.1:8887")
   *       .path("hello")
   *       .GET()
   *       .asString();
   *
   * }</pre>
   *
   * @param url The url effectively replacing the base url.
   * @return The request being built
   * @see HttpClientContext.Builder#withBaseUrl(String)
   */
  HttpClientRequest url(String url);

  /**
   * Add a path segment to the URL.
   *
   * @param path The path segment to add to the URL path.
   * @return The request being built
   */
  HttpClientRequest path(String path);

  /**
   * Add a path segment to the URL.
   *
   * @param val The value to add to the URL path.
   * @return The request being built
   */
  HttpClientRequest path(int val);

  /**
   * Add a path segment to the URL.
   *
   * @param val The value to add to the URL path.
   * @return The request being built
   */
  HttpClientRequest path(long val);

  /**
   * Add a path segment to the URL.
   *
   * @param val The value to add to the URL path.
   * @return The request being built
   */
  HttpClientRequest path(Object val);

  /**
   * Add a matrix parameter to the current path segment.
   *
   * @param name  The matrix parameter name
   * @param value The matrix parameter value which can be null
   * @return The request being built
   */
  HttpClientRequest matrixParam(String name, String value);

  /**
   * Add a matrix parameter to the current path segment.
   *
   * @param name  The matrix parameter name
   * @param value The matrix parameter value which can be null
   * @return The request being built
   */
  HttpClientRequest matrixParam(String name, Object value);

  /**
   * Add a query parameter
   *
   * @param name  The name of the query parameter
   * @param value The value of the query parameter which can be null
   * @return The request being built
   */
  HttpClientRequest queryParam(String name, String value);

  /**
   * Add a query parameter
   *
   * @param name  The name of the query parameter
   * @param value The value of the query parameter which can be null
   * @return The request being built
   */
  HttpClientRequest queryParam(String name, Object value);

  /**
   * Add a multiple query parameters as name value map.
   *
   * @param params The query parameters
   * @return The request being built
   */
  HttpClientRequest queryParam(Map<String, ?> params);

  /**
   * Add a form parameter.
   *
   * @param name  The form parameter name
   * @param value The form parameter value which can be null
   * @return The request being built
   */
  HttpClientRequest formParam(String name, String value);

  /**
   * Add a form parameter.
   *
   * @param name  The form parameter name
   * @param value The form parameter value which can be null
   * @return The request being built
   */
  HttpClientRequest formParam(String name, Object value);

  /**
   * Add the form parameters via a map.
   *
   * @param params The form parameters as name value map
   * @return The request being built
   */
  HttpClientRequest formParam(Map<String, ?> params);

  /**
   * Set encoded body content.
   */
  HttpClientRequest body(BodyContent bodyContent);

  /**
   * Set the body as a bean with the given content type using a BodyWriter.
   */
  HttpClientRequest body(Object bean, String contentType);

  /**
   * Set the body as a bean using the default content type. The default
   * content type will often be <code>application/json; charset=utf8</code>.
   */
  HttpClientRequest body(Object bean);

  /**
   * Set the body content as a string.
   *
   * @param body The body content
   * @return The request being built
   */
  HttpClientRequest body(String body);

  /**
   * Set the body content as a bytes.
   *
   * @param body The body content
   * @return The request being built
   */
  HttpClientRequest body(byte[] body);

  /**
   * Set the body content with supplied InputStream.
   *
   * @param supplier The supplier of InputStream content to send as body content
   * @return The request being built
   */
  HttpClientRequest body(Supplier<? extends InputStream> supplier);

  /**
   * Set the body content with supplied InputStream.
   *
   * @param file The file to send as body content
   * @return The request being built
   */
  HttpClientRequest body(Path file);

  /**
   * Set the body content using http BodyPublisher.
   *
   * @param body The body content
   * @return The request being built
   */
  HttpClientRequest body(HttpRequest.BodyPublisher body);

  /**
   * Deprecated migrate to GET().
   */
  @Deprecated
  default HttpClientResponse get() {
    return GET();
  }

  /**
   * Deprecated migrate to POST().
   */
  @Deprecated
  default HttpClientResponse post() {
    return POST();
  }

  /**
   * Deprecated migrate to PUT().
   */
  @Deprecated
  default HttpClientResponse put() {
    return PUT();
  }

  /**
   * Deprecated migrate to PATCH().
   */
  @Deprecated
  default HttpClientResponse patch() {
    return PATCH();
  }

  /**
   * Deprecated migrate to DELETE().
   */
  @Deprecated
  default HttpClientResponse delete() {
    return DELETE();
  }

  /**
   * Execute the request as a GET.
   */
  HttpClientResponse GET();

  /**
   * Execute the request as a POST.
   */
  HttpClientResponse POST();

  /**
   * Execute the request as a PUT.
   */
  HttpClientResponse PUT();

  /**
   * Execute the request as a PATCH.
   */
  HttpClientResponse PATCH();

  /**
   * Execute the request as a DELETE.
   */
  HttpClientResponse DELETE();

  /**
   * Execute the request as a TRACE.
   */
  HttpClientResponse TRACE();

  /**
   * Execute the request as a HEAD.
   */
  HttpClientResponse HEAD();

  /**
   * After the response is returned this method returns the response time in microseconds.
   * <p>
   * This is useful for use in {@link RequestIntercept#afterResponse(HttpResponse, HttpClientRequest)}
   */
  long responseTimeMicros();
}
