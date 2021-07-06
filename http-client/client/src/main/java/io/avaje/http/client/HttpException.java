package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * HTTP Exception with support for converting the error response body into a bean.
 * <p>
 * Wraps an underlying HttpResponse with helper methods to get the response body
 * as string or as a bean.
 *
 * <h3>Example catching HttpException</h3>
 * <pre>{@code
 *
 *   try {
 *       clientContext.request()
 *         .path("hello/saveForm")
 *         .formParam("email", "user@foo.com")
 *         .formParam("url", "notAValidUrl")
 *         .POST()
 *         .asVoid();
 *
 *     } catch (HttpException e) {
 *
 *       // obtain the statusCode from the exception ...
 *       int statusCode = e.getStatusCode());
 *
 *       HttpResponse<?> httpResponse = e.getHttpResponse();
 *
 *       // obtain the statusCode from httpResponse ...
 *       int statusCode = httpResponse.statusCode();
 *
 *       // convert error response body into a bean (typically Jackson/Gson)
 *       final MyErrorBean errorResponse = e.bean(MyErrorBean.class);
 *
 *       final Map<String, String> errorMap = errorResponse.getErrors();
 *       assertThat(errorMap.get("url")).isEqualTo("must be a valid URL");
 *       assertThat(errorMap.get("name")).isEqualTo("must not be null");
 *     }
 *
 * }</pre>
 */
public class HttpException extends RuntimeException {

  private final int statusCode;
  private final boolean responseAsBytes;
  private DHttpClientContext context;
  private HttpResponse<?> httpResponse;

  /**
   * Create with status code and message.
   */
  public HttpException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
    this.responseAsBytes = false;
  }

  /**
   * Create with status code, message and throwable.
   */
  public HttpException(int statusCode, String message, Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
    this.responseAsBytes = false;
  }

  /**
   * Create with status code and throwable.
   */
  public HttpException(int statusCode, Throwable cause) {
    super(cause);
    this.statusCode = statusCode;
    this.responseAsBytes = false;
  }

  HttpException(HttpResponse<?> httpResponse, DHttpClientContext context) {
    super();
    this.httpResponse = httpResponse;
    this.statusCode = httpResponse.statusCode();
    this.context = context;
    this.responseAsBytes = false;
  }

  HttpException(DHttpClientContext context, HttpResponse<byte[]> httpResponse) {
    super();
    this.httpResponse = httpResponse;
    this.statusCode = httpResponse.statusCode();
    this.context = context;
    this.responseAsBytes = true;
  }

  /**
   * Return the response body content as a bean
   *
   * @param cls The type of bean to convert the response to
   * @return The response as a bean
   */
  public <T> T bean(Class<T> cls) {
    final BodyContent body = context.readErrorContent(responseAsBytes, httpResponse);
    return context.readBean(cls, body);
  }

  /**
   * Return the response body content as a UTF8 string.
   */
  public String bodyAsString() {
    final BodyContent body = context.readErrorContent(responseAsBytes, httpResponse);
    return new String(body.content(), StandardCharsets.UTF_8);
  }

  /**
   * Return the response body content as raw bytes.
   */
  public byte[] bodyAsBytes() {
    final BodyContent body = context.readErrorContent(responseAsBytes, httpResponse);
    return body.content();
  }

  /**
   * Return the HTTP status code.
   */
  public int statusCode() {
    return statusCode;
  }

  /**
   * Deprecated migrate to statusCode()
   */
  @Deprecated
  public int getStatusCode() {
    return statusCode();
  }

  /**
   * Return the underlying HttpResponse.
   */
  public HttpResponse<?> httpResponse() {
    return httpResponse;
  }

  /**
   * Deprecated migrate to httpResponse().
   */
  @Deprecated
  public HttpResponse<?> getHttpResponse() {
    return httpResponse();
  }

}
