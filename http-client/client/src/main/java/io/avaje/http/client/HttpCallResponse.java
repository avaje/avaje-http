package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Stream;

/**
 * Allows the client code to choose to execute the request asynchronously
 * or synchronously.
 */
public interface HttpCallResponse {

  /**
   * Process discarding response body as {@literal HttpResponse<Void>}.
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
   * Call using any given {@code HttpResponse.BodyHandler}.
   * <pre>{@code
   *
   *    HttpCall<E> call = clientContext.request()
   *       .path("hello/lineStream")
   *       .GET()
   *       .call().withHandler(HttpResponse.BodyHandler<E> ...);
   * }</pre>
   *
   * @param bodyHandler The response body handler to use
   * @return The HttpCall to allow sync or async execution
   */
  <E> HttpCall<HttpResponse<E>> withHandler(HttpResponse.BodyHandler<E> bodyHandler);

  /**
   * A bean response to execute async or sync.
   * <p>
   * If the HTTP statusCode is 300 or above a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException. Redirects are
   * by default followed apart from HTTPS to HTTP.
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
   * If the HTTP statusCode is 300 or above a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException. Redirects are
   * by default followed apart from HTTPS to HTTP.
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
   * If the HTTP statusCode is 300 or above a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException. Redirects are
   * by default followed apart from HTTPS to HTTP.
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
