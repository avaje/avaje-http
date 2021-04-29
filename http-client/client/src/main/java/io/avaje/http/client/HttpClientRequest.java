package io.avaje.http.client;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.nio.file.Path;
import java.time.*;
import java.util.UUID;
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
 *       .get().bean(HelloDto.class);
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
   *       .get().asString();
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
  HttpClientRequest path(UUID val);

  /**
   * Add a path segment to the URL.
   *
   * @param val The value to add to the URL path.
   * @return The request being built
   */
  HttpClientRequest path(LocalDate val);

  /**
   * Add a matrix parameter to the current path segment.
   *
   * @param name  The matrix parameter name
   * @param value The matrix parameter value
   * @return The request being built
   */
  HttpClientRequest matrixParam(String name, String value);

  /**
   * Add a query parameter
   *
   * @param name  The name of the query parameter
   * @param value The value of the query parameter
   * @return The request being built
   */
  HttpClientRequest queryParam(String name, String value);

  /**
   * Add a Integer query parameter
   *
   * @param name  The name of the query parameter
   * @param value The value of the query parameter
   * @return The request being built
   */
  HttpClientRequest queryParam(String name, Integer value);

  /**
   * Add a Long query parameter
   *
   * @param name  The name of the query parameter
   * @param value The value of the query parameter
   * @return The request being built
   */
  HttpClientRequest queryParam(String name, Long value);

  /**
   * Add a UUID query parameter
   *
   * @param name  The name of the query parameter
   * @param value The value of the query parameter
   * @return The request being built
   */
  HttpClientRequest queryParam(String name, UUID value);

  /**
   * Add a Boolean query parameter
   *
   * @param name  The name of the query parameter
   * @param value The value of the query parameter
   * @return The request being built
   */
  HttpClientRequest queryParam(String name, Boolean value);

  /**
   * Add a LocalDate query parameter
   *
   * @param name  The name of the query parameter
   * @param value The value of the query parameter
   * @return The request being built
   */
  HttpClientRequest queryParam(String name, LocalDate value);

  /**
   * Add a LocalTime query parameter
   *
   * @param name  The name of the query parameter
   * @param value The value of the query parameter
   * @return The request being built
   */
  HttpClientRequest queryParam(String name, LocalTime value);

  /**
   * Add a LocalDateTime query parameter
   *
   * @param name  The name of the query parameter
   * @param value The value of the query parameter
   * @return The request being built
   */
  HttpClientRequest queryParam(String name, LocalDateTime value);

  /**
   * Add a Instant query parameter as ISO-8601 format.
   *
   * @param name  The name of the query parameter
   * @param value The value of the query parameter
   * @return The request being built
   */
  HttpClientRequest queryParam(String name, Instant value);

  /**
   * Add a form parameter.
   *
   * @param name  The form parameter name
   * @param value The form parameter value
   * @return The request being built
   */
  HttpClientRequest formParam(String name, String value);

  /**
   * Add a Integer value form parameter.
   */
  HttpClientRequest formParam(String name, Integer value);

  /**
   * Add a Long value form parameter.
   */
  HttpClientRequest formParam(String name, Long value);

  /**
   * Add a Long value form parameter.
   */
  HttpClientRequest formParam(String name, UUID value);

  /**
   * Add a Boolean form parameter
   */
  HttpClientRequest formParam(String name, Boolean value);

  /**
   * Add a LocalDate form parametert
   */
  HttpClientRequest formParam(String name, LocalDate value);

  /**
   * Add a LocalTime form parameter
   */
  HttpClientRequest formParam(String name, LocalTime value);

  /**
   * Add a LocalDateTime form parameter
   */
  HttpClientRequest formParam(String name, LocalDateTime value);

  /**
   * Add a Instant form parameter as ISO-8601 format.
   */
  HttpClientRequest formParam(String name, Instant value);

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
  HttpClientRequest body(Path file) throws FileNotFoundException;

  /**
   * Set the body content using http BodyPublisher.
   *
   * @param body The body content
   * @return The request being built
   */
  HttpClientRequest body(HttpRequest.BodyPublisher body);

  /**
   * Execute the request as a GET.
   */
  HttpClientResponse get();

  /**
   * Execute the request as a POST.
   */
  HttpClientResponse post();

  /**
   * Execute the request as a PUT.
   */
  HttpClientResponse put();

  /**
   * Execute the request as a DELETE.
   */
  HttpClientResponse delete();

}
