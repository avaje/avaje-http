package io.avaje.http.client;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Async processing of the request with responses as CompletableFuture.
 *
 * <h4>Testing and .join()</h4>
 * <p>
 * Note that when testing with async requests we frequently use {@code .join()}
 * on the {@code CompletableFuture} such that the main thread waits for the async
 * processing to complete. After that various asserts can run knowing that the
 * async callback code has been executed.
 *
 * <h4>Example using .join() for testing purposes</h4>
 * <pre>{@code
 *
 *    client.request()
 *       ...
 *       .POST().async()
 *       .bean(HelloDto.class)
 *       .whenComplete((helloDto, throwable) -> {
 *         ...
 *       }).join(); // wait for async processing to complete
 *
 *       // can assert now ...
 *       assertThat(...)
 *
 * }</pre>
 *
 * <h4>Example async().bean()</h4>
 * <p>
 * In this example POST async that will return a bean converted from json response.
 * <pre>{@code
 *
 *    client.request()
 *       ...
 *       .POST().async()
 *       .bean(HelloDto.class)
 *       .whenComplete((helloDto, throwable) -> {
 *
 *         if (throwable != null) {
 *           HttpException httpException = (HttpException) throwable.getCause();
 *           int statusCode = httpException.statusCode();
 *
 *           // maybe convert json error response body to a bean (using Jackson/Gson)
 *           MyErrorBean errorResponse = httpException.bean(MyErrorBean.class);
 *           ..
 *
 *         } else {
 *           // process helloDto
 *           ...
 *         }
 *       });
 * }</pre>
 */
public interface HttpAsyncResponse {

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
   *   client.request()
   *       .path("hello/world")
   *       .GET()
   *       .async().asVoid()
   *       .whenComplete((hres, throwable) -> {
   *
   *         if (throwable != null) {
   *
   *           // if throwable.getCause() is a HttpException for status code >= 300
   *           HttpException httpException = (HttpException) throwable.getCause();
   *           int status = httpException.statusCode();
   *
   *           // convert json error response body to a bean
   *           ErrorResponse errorResponse = httpException.bean(ErrorResponse.class);
   *           ...
   *         } else {
   *           int statusCode = hres.statusCode();
   *           ...
   *         }
   *       });
   *
   * }</pre>
   */
  CompletableFuture<HttpResponse<Void>> asVoid();

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
   *   client.request()
   *       .path("hello/world")
   *       .GET()
   *       .async().asDiscarding()
   *       .whenComplete((hres, throwable) -> {
   *
   *         if (throwable != null) {
   *           ...
   *         } else {
   *           int statusCode = hres.statusCode();
   *           ...
   *         }
   *       });
   *
   * }</pre>
   *
   * @return The CompletableFuture of the response
   */
  CompletableFuture<HttpResponse<Void>> asDiscarding();

  /**
   * Process as String response body {@literal HttpResponse<String>}.
   *
   * <pre>{@code
   *
   *   client.request()
   *       .path("hello/world")
   *       .GET()
   *       .async().asString()
   *       .whenComplete((hres, throwable) -> {
   *
   *         if (throwable != null) {
   *           ...
   *         } else {
   *           int statusCode = hres.statusCode();
   *           String body = hres.body();
   *           ...
   *         }
   *       });
   *
   * }</pre>
   *
   * @return The CompletableFuture of the response
   */
  CompletableFuture<HttpResponse<String>> asString();

  /**
   * Process as response {@literal HttpResponse<byte[]>}.
   *
   * @return The CompletableFuture of the response
   */
  CompletableFuture<HttpResponse<byte[]>> asByteArray();

  /**
   * Process as response {@literal HttpResponse<Stream<String>>}.
   *
   * @return The CompletableFuture of the response
   */
  CompletableFuture<HttpResponse<Stream<String>>> asLines();

  /**
   * Process as response {@literal HttpResponse<InputStream>}.
   *
   * @return The CompletableFuture of the response
   */
  CompletableFuture<HttpResponse<InputStream>> asInputStream();

  /**
   * Process with any given {@code HttpResponse.BodyHandler}.
   *
   * <h3>Example: line subscriber</h3>
   * <p>
   * Subscribe line by line to the response.
   * </p>
   * <pre>{@code
   *
   *    CompletableFuture<HttpResponse<Void>> future = client.request()
   *       .path("hello/lineStream")
   *       .GET().async()
   *       .handler(HttpResponse.BodyHandlers.fromLineSubscriber(new Flow.Subscriber<>() {
   *
   *         @Override
   *         public void onSubscribe(Flow.Subscription subscription) {
   *           subscription.request(Long.MAX_VALUE);
   *         }
   *         @Override
   *         public void onNext(String item) {
   *           ...
   *         }
   *         @Override
   *         public void onError(Throwable throwable) {
   *           ...
   *         }
   *         @Override
   *         public void onComplete() {
   *           ...
   *         }
   *       }))
   *       .whenComplete((hres, throwable) -> {
   *         int statusCode = hres.statusCode();
   *         ...
   *       });
   * }</pre>
   *
   * @param bodyHandler The body handler to use to process the response
   * @return The CompletableFuture of the response
   */
  <E> CompletableFuture<HttpResponse<E>> handler(HttpResponse.BodyHandler<E> bodyHandler);

  /**
   * Deprecated - migrate to handler().
   */
  @Deprecated
  default <E> CompletableFuture<HttpResponse<E>> withHandler(HttpResponse.BodyHandler<E> bodyHandler) {
    return handler(bodyHandler);
  }

  /**
   * Process converting the response body to the given type.
   * <p>
   * If the HTTP statusCode is not in the 2XX range a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException.
   *
   * <pre>{@code
   *
   *    client.request()
   *       ...
   *       .POST().async()
   *       .as(HelloDto.class)
   *       .whenComplete((helloResponse, throwable) -> {
   *
   *         if (throwable != null) {
   *           HttpException httpException = (HttpException) throwable.getCause();
   *           int statusCode = httpException.statusCode();
   *
   *           // maybe convert json error response body to a bean (using Jackson/Gson)
   *           MyErrorBean errorResponse = httpException.bean(MyErrorBean.class);
   *           ..
   *
   *         } else {
   *           int statusCode = helloResponse.statusCode();
   *           HelloDto helloDto = helloResponse.body();
   *           ...
   *         }
   *       });
   * }</pre>
   *
   * @param type The bean type to convert the content to
   * @return The CompletableFuture of the response
   */
  <E> CompletableFuture<HttpResponse<E>> as(Class<E> type);

  /**
   * The same as {@link #as(Class)} but using a generic type.
   */
  <E> CompletableFuture<HttpResponse<E>> as(Type type);

  /**
   * Process expecting a bean response body (typically from json content).
   * <p>
   * If the HTTP statusCode is not in the 2XX range a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException.
   *
   * <pre>{@code
   *
   *    client.request()
   *       ...
   *       .POST().async()
   *       .bean(HelloDto.class)
   *       .whenComplete((helloDto, throwable) -> {
   *
   *         if (throwable != null) {
   *           HttpException httpException = (HttpException) throwable.getCause();
   *           int statusCode = httpException.statusCode();
   *
   *           // maybe convert json error response body to a bean (using Jackson/Gson)
   *           MyErrorBean errorResponse = httpException.bean(MyErrorBean.class);
   *           ..
   *
   *         } else {
   *           // process helloDto
   *           ...
   *         }
   *       });
   * }</pre>
   *
   * @param type The bean type to convert the content to
   * @return The CompletableFuture of the response
   */
  <E> CompletableFuture<E> bean(Class<E> type);

  /**
   * Process converting the response body to a list of the given type.
   * <p>
   * If the HTTP statusCode is not in the 2XX range a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException.
   *
   * <pre>{@code
   *
   *    client.request()
   *       ...
   *       .POST().async()
   *       .asList(HelloDto.class)
   *       .whenComplete((helloResponse, throwable) -> {
   *
   *         if (throwable != null) {
   *           // error response
   *           HttpException httpException = (HttpException) throwable.getCause();
   *           int statusCode = httpException.statusCode();
   *
   *           // maybe convert json error response body to a bean (using Jackson/Gson)
   *           MyErrorBean errorResponse = httpException.bean(MyErrorBean.class);
   *           ..
   *
   *         } else {
   *           // success response
   *           int statusCode = helloResponse.statusCode();
   *           List<HelloDto> body = helloResponse.body();
   *           ...
   *         }
   *       });
   * }</pre>
   *
   * @param type The type to convert the content to
   * @return The CompletableFuture of the response
   */
  <E> CompletableFuture<HttpResponse<List<E>>> asList(Class<E> type);

  /**
   * The same as {@link #asList(Class)} but using a generic type.
   */
  <E> CompletableFuture<HttpResponse<List<E>>> asList(Type type);

  /**
   * Process expecting a list of beans response body (typically from json content).
   * <p>
   * If the HTTP statusCode is not in the 2XX range a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException.
   *
   * <pre>{@code
   *
   *    client.request()
   *       ...
   *       .GET().async()
   *       .list(HelloDto.class)
   *       .whenComplete((helloDtos, throwable) -> {
   *
   *         if (throwable != null) {
   *           HttpException httpException = (HttpException) throwable.getCause();
   *           int statusCode = httpException.statusCode();
   *           ...
   *
   *         } else {
   *           // process list of helloDto
   *           ...
   *         }
   *       });
   * }</pre>
   *
   * @param type The bean type to convert the content to
   * @return The CompletableFuture of the response
   */
  <E> CompletableFuture<List<E>> list(Class<E> type);

  /**
   * Process converting the response body to a stream of the given type.
   * <p>
   * If the HTTP statusCode is not in the 2XX range a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException.
   *
   * <pre>{@code
   *
   *    client.request()
   *       ...
   *       .POST().async()
   *       .asStream(HelloDto.class)
   *       .whenComplete((helloResponse, throwable) -> {
   *
   *         if (throwable != null) {
   *           // error response
   *           HttpException httpException = (HttpException) throwable.getCause();
   *           int statusCode = httpException.statusCode();
   *
   *           // maybe convert json error response body to a bean (using Jackson/Gson)
   *           MyErrorBean errorResponse = httpException.bean(MyErrorBean.class);
   *           ..
   *
   *         } else {
   *           // success response
   *           int statusCode = helloResponse.statusCode();
   *           Stream<HelloDto> body = helloResponse.body();
   *           ...
   *         }
   *       });
   * }</pre>
   *
   * @param type The type to convert the content to
   * @return The CompletableFuture of the response
   */
  <E> CompletableFuture<HttpResponse<Stream<E>>> asStream(Class<E> type);

  /**
   * The same as {@link #asStream(Class)} but using a generic type.
   */
  <E> CompletableFuture<HttpResponse<Stream<E>>> asStream(Type type);

  /**
   * Process response as a stream of beans (x-json-stream).
   * <p>
   * Typically the response is expected to be {@literal application/x-json-stream}
   * newline delimited json payload.
   * <p>
   * Note that for this stream request the response content is not deemed
   * 'loggable' by avaje-http-client. This is because the entire response
   * may not be available at the time of the callback. As such {@link RequestLogger}
   * will not include response content in logging stream request/response.
   * <p>
   * If the HTTP statusCode is not in the 2XX range a HttpException is throw which contains
   * the HttpResponse. This is the cause in the CompletionException.
   *
   * <pre>{@code
   *
   *   CompletableFuture<Stream<Customer>> future = clientContext.request()
   *       .path("customers/stream")
   *       .GET().async()
   *       .stream(Customer.class);
   *
   *   future.whenComplete((stream, throwable) -> {
   *       // if throwable != null ... handle error
   *
   *       // else process Stream<Customer> ...
   *       try (stream) {
   *         stream.forEach(customer -> {
   *           ...
   *         });
   *       }
   *     });
   *
   * }</pre>
   *
   * @param type The bean type to convert the content to
   * @return The CompletableFuture of the response
   */
  <E> CompletableFuture<Stream<E>> stream(Class<E> type);

  /**
   * Process expecting a bean response body (typically from json content).
   *
   * @param type The type to convert the content to
   * @return The CompletableFuture of the response
   */
  <E> CompletableFuture<E> bean(Type type);

  /**
   * Process expecting a list of beans response body (typically from json content).
   *
   * @param type The type to convert the content to
   * @return The CompletableFuture of the response
   */
  <E> CompletableFuture<List<E>> list(Type type);

  /**
   * Process response as a stream of beans (x-json-stream).
   *
   * @param type The type to convert the content to
   * @return The CompletableFuture of the response
   */
  <E> CompletableFuture<Stream<E>> stream(Type type);

}
