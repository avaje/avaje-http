package io.avaje.http.client;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
 *  HelloDto dto = client.request()
 *       .path("hello").queryParam("name", "Rob").queryParam("say", "Whats up")
 *       .GET()
 *       .bean(HelloDto.class);
 *
 * }</pre>
 *
 * @see HttpClient
 */
public interface HttpClientRequest extends Cloneable {

  /**
   * Return a copy of the HttpClientRequest.
   */
  HttpClientRequest clone();

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
   * Set a label for the request. The label is intended to be used to group and
   * identify metrics for the request.
   *
   * @param label The label that can be used to identify metrics for the request
   */
  HttpClientRequest label(String label);

  /**
   * Return the label that has been set on this request.
   * <p>
   * Typically the label would be read in {@link RequestIntercept#afterResponse(HttpResponse, HttpClientRequest)}
   * to assign request execution metrics.
   */
  String label();

  /**
   * Used to pass custom attribute between {@link RequestIntercept} methods.
   * <p>
   * Allows us to pass something between {@code beforeRequest} and {@code afterResponse}
   * methods of a {@link RequestIntercept} or between multiple {@link RequestIntercept}.
   *
   * @param key   The unique key used to store the attribute
   * @param value The attribute to store
   */
  HttpClientRequest setAttribute(String key, Object value);

  /**
   * Return a custom attribute typically set by a {@link RequestIntercept#beforeRequest(HttpClientRequest)}.
   *
   * @param key The key for the custom attribute
   * @param <E> The inferred type of the attribute
   * @return The custom attribute
   */
  <E> E getAttribute(String key);

  /**
   * Set the request timeout to use for this request. When not set the default
   * request timeout will be used.
   *
   * @param requestTimeout The request timeout to use for this request.
   * @return The request being built
   */
  HttpClientRequest requestTimeout(Duration requestTimeout);

  /**
   * Add the header to the request but only if there is no existing value for the given header.
   *
   * @param name  The header name
   * @param value The header value
   * @return The request being built
   */
  HttpClientRequest headerAddIfAbsent(String name, Object value);

  /**
   * Add the header to the request.
   *
   * @param name  The header name
   * @param value The header value
   * @return The request being built
   */
  HttpClientRequest header(String name, String value);

  /**
   * Add the header to the request implicitly converting the value to a String. If the value is a
   * collection then it's values are appended with the same key
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
   * Add the headers to the request via Collection.
   *
   * @param name  The header name
   * @param value The header values
   * @return The request being built
   */
  HttpClientRequest header(String name, Collection<String> value);

  /**
   * Return the header values that have been set for the given header name.
   *
   * @return The headers values or an empty collection if the header has not been specified yet.
   */
  List<String> header(String name);

  /**
   * Return the header values that have been set for this request.
   *
   * @return The headers values or an empty map if no headers have been specified yet.
   */
  Map<String, List<String>> headers();

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
   *  HttpResponse<String> res = client.request()
   *       .url("http://127.0.0.1:8889")
   *       .path("hello")
   *       .GET()
   *       .asString();
   *
   * }</pre>
   *
   * @param url The url effectively replacing the base url.
   * @return The request being built
   * @see HttpClient.Builder#baseUrl(String)
   */
  HttpClientRequest url(String url);

  /**
   * The Http Verb (GET, POST, PUT etc) of this request.
   *
   * @return The Http Verb of this request.
   */
  String method();

  /**
   * The URL for this request including the query parameters.
   *
   * @return The url for this request
   */
  String url();

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
   * Add a query parameter, if value is a collection then it's values are appended with the same key
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
   * Add a query parameter with multiple values
   *
   * @param name   The name of the query parameter
   * @param values The values of the query parameter which can be null
   * @return The request being built
   */
  default HttpClientRequest queryParam(String name, Collection<String> values) {
    return queryParam(name, (Object) values);
  }

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
   * Set the body as a bean using the default content type.
   * <p>
   * The default content type will often be {@code application/json; charset=utf8}.
   */
  HttpClientRequest body(Object bean);

  /**
   * Set the body as a bean additionally specifying the type that will be
   * used to serialise the content (e.g. JsonbAdapter).
   * <p>
   * Specifying the type allows the bean instance to be a type that extends
   * a type that is known to JsonbAdapter / the body content adapter used.
   *
   * @param bean The body content as an instance
   * @param type The type used by the body content adapter to write the body content
   * @return The request being built
   */
  HttpClientRequest body(Object bean, Class<?> type);

  /**
   * Set the body as a bean additionally specifying the type that will be
   * used to serialise the content (e.g. JsonbAdapter).
   * <p>
   * Specifying the type allows the bean instance to be a type that extends
   * a type that is known to JsonbAdapter / the body content adapter used.
   *
   * @param bean The body content as an instance
   * @param type The type used by the body content adapter to write the body content
   * @return The request being built
   */
  HttpClientRequest body(Object bean, Type type);

  /**
   * Set the body as a bean with the given content type and additionally specifying
   * the type that will be used to serialise the content (e.g. JsonbAdapter).
   * <p>
   * Specifying the type allows the bean instance to be a type that extends
   * a type that is known to JsonbAdapter / the body content adapter used.
   *
   * @param bean        The body content as an instance
   * @param type        The type used by the body content adapter to write the body content
   * @param contentType The content type of the body
   * @return The request being built
   */
  HttpClientRequest body(Object bean, Class<?> type, String contentType);

  /**
   * Set the body content as a string using the default content type.
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
   * @param stream The InputStream content to send as body content
   * @return The request being built
   */
  HttpClientRequest body(InputStream stream);

  /**
   * Set the body content with supplied InputStream.
   *
   * @param file The file to send as body content
   * @return The request being built
   */
  HttpClientRequest body(Path file);


 /**
  * Set the body content using a callback that writes to an {@link java.io.OutputStream}.
  * <p>
  * This allows streaming large or dynamically generated content directly to the HTTP request body,
  * without buffering the entire payload in memory. The provided {@code OutputStreamWriter} is called
  * with an {@link java.io.OutputStream} that writes to the request body. Data written to the stream
  * is sent as the request body.
  * <p>
  * Example usage:
  * <pre>{@code
  *   client.request()
  *     .url("http://example.com/upload")
  *     .body(outputStream -> {
  *       // Write data in chunks
  *       for (byte[] chunk : getChunks()) {
  *         outputStream.write(chunk);
  *       }
  *     })
  *     .POST()
  *     .asPlainString();
  * }</pre>
  *
  * @param writer Callback to write data to the request body output stream
  * @return The request being built
  */
  HttpClientRequest body(OutputStreamBodyWriter writer);

  /**
   * Set the body content using http BodyPublisher.
   *
   * @param body The body content
   * @return The request being built
   */
  HttpClientRequest body(HttpRequest.BodyPublisher body);

  /**
   * Get the body content for this request if available. Will return an empty optional for streaming calls
   *
   * @return The request body
   */
  Optional<BodyContent> bodyContent();

  /**
   * Set the mapper used to transform {@link HttpException} into a different kind of exception.
   *
   * <p>When set, all {@link HttpException} that are thrown by this request will be caught and
   * transformed into more specific exception with the given function by default.
   *
   * @param errorMapper function to map the httpException
   * @return The request being built
   */
  HttpClientRequest errorMapper(Function<HttpException, RuntimeException> errorMapper);

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
   * Execute the request using the given http method.
   *
   * @param method The http method to execute the request with (e.g. GET, POST, PUT, PATCH, DELETE,
   *     TRACE, HEAD)
   */
  default HttpClientResponse httpMethod(String method) {
    switch (method.toUpperCase()) {
      case "GET":
        return GET();
      case "POST":
        return POST();
      case "PUT":
        return PUT();
      case "PATCH":
        return PATCH();
      case "DELETE":
        return DELETE();
      case "TRACE":
        return TRACE();
      case "HEAD":
        return HEAD();
      default:
        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }
  }

  /**
   * After the response is returned this method returns the response time in microseconds.
   * <p>
   * This is useful for use in {@link RequestIntercept#afterResponse(HttpResponse, HttpClientRequest)}
   */
  long responseTimeMicros();

}
