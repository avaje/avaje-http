package io.avaje.http.client;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Stream;

/**
 * Allows the client code to choose to execute the request asynchronously
 * or synchronously.
 */
public interface HttpCallResponse {

  /**
   * Process the response with check for 200 range status code
   * returning as {@literal HttpResponse<Void>}.
   * <p>
   * Unlike {@link #asDiscarding()} this request will read any response
   * content as bytes with the view that the response content can be
   * an error message that could be read via for example
   * {@link HttpException#bean(Class)}.
   * <p>
   * Will throw an HttpException if the status code is in the
   * error range allowing the caller to access the error message
   * body via for example {@link HttpException#bean(Class)}
   * <p>
   * This is intended to be used for POST, PUT, DELETE requests
   * where the caller is only interested in the response body
   * when an error occurs (status code not in 200 range).
   *
   * <pre>{@code
   *
   *   HttpCall<HttpResponse<Void>> call =
   *     clientContext.request()
   *       .path("hello/world")
   *       .GET()
   *       .call().asVoid();
   *
   * }</pre>
   *
   * @return The HttpCall to allow sync or async execution
   */
  HttpCall<HttpResponse<Void>> asVoid();

  /**
   * Process discarding response body as {@literal HttpResponse<Void>}.
   * <p>
   * Unlike {@link #asVoid()} this will discard any response body including
   * any error response body. We should instead use {@link #asVoid()} if we
   * might get an error response body that we want to read via
   * for example {@link HttpException#bean(Class)}.
   *
   * <pre>{@code
   *
   *   HttpCall<HttpResponse<Void>> call =
   *     clientContext.request()
   *       .path("hello/world")
   *       .GET()
   *       .call().asDiscarding();
   *
   * }</pre>
   *
   * @return The HttpCall to allow sync or async execution
   */
  HttpCall<HttpResponse<Void>> asDiscarding();

  /**
   * Process as String response body {@literal HttpResponse<String>}.
   *
   * <pre>{@code
   *
   *   HttpCall<HttpResponse<String>> call =
   *     clientContext.request()
   *       .path("hello/world")
   *       .GET()
   *       .call().asString();
   *
   * }</pre>
   *
   * @return The HttpCall to allow sync or async execution
   */
  HttpCall<HttpResponse<String>> asString();

  /**
   * Process as response {@literal HttpResponse<byte[]>}.
   *
   * @return The CompletableFuture of the response
   */
  HttpCall<HttpResponse<byte[]>> asByteArray();

  /**
   * Process as response {@literal HttpResponse<Stream<String>>}.
   *
   * @return The CompletableFuture of the response
   */
  HttpCall<HttpResponse<Stream<String>>> asLines();

  /**
   * Process as response {@literal HttpResponse<InputStream>}.
   *
   * @return The CompletableFuture of the response
   */
  HttpCall<HttpResponse<InputStream>> asInputStream();

  /**
   * Call using any given {@code HttpResponse.BodyHandler}.
   * <pre>{@code
   *
   *    HttpCall<E> call = clientContext.request()
   *       .path("hello/lineStream")
   *       .GET()
   *       .call().handler(HttpResponse.BodyHandler<E> ...);
   * }</pre>
   *
   * @param bodyHandler The response body handler to use
   * @return The HttpCall to allow sync or async execution
   */
  <E> HttpCall<HttpResponse<E>> handler(HttpResponse.BodyHandler<E> bodyHandler);

  /**
   * Deprecated - migrate to handler().
   */
  @Deprecated
  default <E> HttpCall<HttpResponse<E>> withHandler(HttpResponse.BodyHandler<E> bodyHandler) {
    return handler(bodyHandler);
  }

  /**
   * A bean response to execute async or sync.
   * <p>
   * If the HTTP statusCode is not in the 2XX range a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException.
   *
   * <pre>{@code
   *
   *  HttpCall<HelloDto> call =
   *    clientContext.request()
   *       ...
   *       .POST()
   *       .call().bean(HelloDto.class);
   *
   * }</pre>
   *
   * @param type The bean type to convert the content to
   * @return The HttpCall to allow sync or async execution
   */
  <E> HttpCall<E> bean(Class<E> type);

  /**
   * Process expecting a list of beans response body (typically from json content).
   * <p>
   * If the HTTP statusCode is not in the 2XX range a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException.
   *
   * <pre>{@code
   *
   *  HttpCall<List<HelloDto>> call =
   *    clientContext.request()
   *       ...
   *       .GET()
   *       .call().list(HelloDto.class);
   * }</pre>
   *
   * @param type The bean type to convert the content to
   * @return The HttpCall to execute sync or async
   */
  <E> HttpCall<List<E>> list(Class<E> type);

  /**
   * Process expecting a stream of beans response body (typically from json content).
   * <p>
   * If the HTTP statusCode is not in the 2XX range a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException.
   *
   * <pre>{@code
   *
   *  HttpCall<Stream<HelloDto>> call =
   *    clientContext.request()
   *       ...
   *       .GET()
   *       .call().stream(HelloDto.class);
   * }</pre>
   *
   * @param type The bean type to convert the content to
   * @return The HttpCall to execute sync or async
   */
  <E> HttpCall<Stream<E>> stream(Class<E> type);

}
