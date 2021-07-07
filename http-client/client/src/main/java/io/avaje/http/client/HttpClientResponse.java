package io.avaje.http.client;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Controls how the response is processed including potential
 * conversion into beans.
 */
public interface HttpClientResponse {

  /**
   * Send the request async using CompletableFuture.
   */
  HttpAsyncResponse async();

  /**
   * Return a HttpCall which allows either sync or async
   * execution of the request.
   */
  HttpCallResponse call();

  /**
   * Returning the response using the given response reader.
   *
   * @param reader The response reader.
   * @param <T>    The type that the content is converted to.
   * @return The response converted into the appropriate bean via the reader.
   * @throws HttpException when the response has error status codes
   */
  <T> T read(BodyReader<T> reader);

  /**
   * Return the response as a single bean.
   * <p>
   * If the HTTP statusCode is 300 or above a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException. Redirects are
   * by default followed apart from HTTPS to HTTP.
   *
   * @param type The type of the bean to convert the response content into.
   * @param <T>  The type that the content is converted to.
   * @return The bean the response is converted into.
   * @throws HttpException when the response has error status codes
   */
  <T> T bean(Class<T> type);

  /**
   * Return the response as a list of beans.
   * <p>
   * If the HTTP statusCode is 300 or above a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException. Redirects are
   * by default followed apart from HTTPS to HTTP.
   *
   * @param type The type of the bean to convert the response content into.
   * @param <T>  The type that the content is converted to.
   * @return The list of beans the response is converted into.
   * @throws HttpException when the response has error status codes
   */
  <T> List<T> list(Class<T> type);

  /**
   * Return the response as a stream of beans.
   * <p>
   * Typically the response is expected to be {@literal application/x-json-stream}
   * newline delimited json payload.
   * <p>
   * Note that for this stream request the response content is not deemed
   * 'loggable' by avaje-http-client. This is because the entire response
   * may not be available at the time of the callback. As such {@link RequestLogger}
   * will not include response content when logging stream request/response
   * <p>
   * If the HTTP statusCode is 300 or above a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException. Redirects are
   * by default followed apart from HTTPS to HTTP.
   *
   * @param type The type of the bean to convert the response content into.
   * @param <T>  The type that the content is converted to.
   * @return The stream of beans from the response
   * @throws HttpException when the response has error status codes
   */
  <T> Stream<T> stream(Class<T> type);

  /**
   * Return the response with check for 200 range status code.
   * <p>
   * Will throw an HttpException if the status code is in the
   * error range allowing the caller to access the error message
   * body via {@link HttpException#bean(Class)}
   * <p>
   * This is intended to be used for POST, PUT, DELETE requests
   * where the caller is only interested in the response body
   * when an error occurs (status code not in 200 range).
   *
   * @throws HttpException when the response has error status codes
   */
  HttpResponse<Void> asVoid();

  /**
   * Return the response discarding the response content.
   * <p>
   * Unlike {@link #asVoid()} this will discard any response body including
   * any error response body. We should instead use {@link #asVoid()} if we
   * might get an error response body that we want to read via
   * for example {@link HttpException#bean(Class)}.
   */
  HttpResponse<Void> asDiscarding();

  /**
   * Return the content as string.
   */
  HttpResponse<String> asString();

  /**
   * Return the content as string with check for 200 range status code.
   * <p>
   * If the status code is in the error range then a {@link HttpException}
   * is thrown.
   */
  HttpResponse<String> asPlainString();

  /**
   * Return the content as InputStream.
   */
  HttpResponse<InputStream> asInputStream();

  /**
   * Return the content as a stream of string lines.
   */
  HttpResponse<Stream<String>> asLines();

  /**
   * Return the content as byte array.
   */
  HttpResponse<byte[]> asByteArray();

  /**
   * Return the content into the given file.
   */
  HttpResponse<Path> asFile(Path file);

  /**
   * Return the response using the given response body handler.
   */
  <T> HttpResponse<T> withHandler(HttpResponse.BodyHandler<T> responseHandler);

}
