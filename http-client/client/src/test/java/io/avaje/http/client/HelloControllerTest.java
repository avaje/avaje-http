package io.avaje.http.client;

import org.example.webserver.ErrorResponse;
import org.example.webserver.HelloDto;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HelloControllerTest extends BaseWebTest {

  final HttpClientContext clientContext = client();

  @Test
  void queryParamMap() {
    Map<String, String> params = new LinkedHashMap<>();
    params.put("A", "a");
    params.put("B", "b");

    final HttpResponse<String> hres = clientContext.request()
      .path("hello").path("message")
      .queryParam(params)
      .GET().asString();

    assertThat(hres.statusCode()).isEqualTo(200);
    assertThat(hres.uri().toString()).isEqualTo("http://localhost:8887/hello/message?A=a&B=b");
  }

  @Test
  void asLines() {
    final HttpResponse<Stream<String>> hres =
      clientContext.request()
        .path("hello").path("stream")
        .GET()
        .asLines();

    assertThat(hres.statusCode()).isEqualTo(200);
    final List<String> lines = hres.body().collect(Collectors.toList());

    assertThat(lines).hasSize(4);
    assertThat(lines.get(0)).contains("{\"id\":1, \"name\":\"one\"}");
  }

  @Test
  void asInputStream() throws IOException {
    final HttpResponse<InputStream> hres =
      clientContext.request()
        .path("hello").path("stream")
        .GET()
        .asInputStream();

    assertThat(hres.statusCode()).isEqualTo(200);
    final LineNumberReader reader = new LineNumberReader(new InputStreamReader(hres.body()));

    List<String> allLines = new ArrayList<>();
    String line;
    while ((line = reader.readLine()) != null) {
      allLines.add(line);
    }
    assertThat(allLines).hasSize(4);
    assertThat(allLines.get(0)).contains("{\"id\":1, \"name\":\"one\"}");
  }

  @Test
  void asFile() throws IOException {
    final Path path = File.createTempFile("test", "dump").toPath();
    final HttpResponse<Path> hres =
      clientContext.request()
        .path("hello").path("stream")
        .GET()
        .asFile(path);

    assertThat(hres.statusCode()).isEqualTo(200);
    assertThat(path.toFile().length()).isGreaterThan(90L);

    assertTrue(path.toFile().delete());
  }

  @Test
  void get_stream() {

    final Stream<SimpleData> stream = clientContext.request()
      .path("hello").path("stream")
      .GET()
      .stream(SimpleData.class);

    final List<SimpleData> data = stream.collect(Collectors.toList());

    assertThat(data).hasSize(4);
    final SimpleData first = data.get(0);
    assertThat(first.id).isEqualTo(1);
    assertThat(first.name).isEqualTo("one");
  }

  @Test
  void get_stream_NotFoundException() {
    final HttpException httpException = assertThrows(HttpException.class, () ->
      clientContext.request()
        .path("this-path-does-not-exist")
        .GET()
        .stream(SimpleData.class));

    assertThat(httpException.statusCode()).isEqualTo(404);
    assertThat(httpException.httpResponse().statusCode()).isEqualTo(404);
  }

  @Test
  void async_get_stream() throws ExecutionException, InterruptedException {

    final CompletableFuture<Stream<SimpleData>> future = clientContext.request()
      .path("hello").path("stream")
      .GET().async()
      .stream(SimpleData.class);

    future.whenComplete((stream, throwable) -> {
      assertThat(throwable).isNull();

      final List<SimpleData> data = stream.collect(Collectors.toList());
      assertThat(data).hasSize(4);
      final SimpleData first = data.get(0);
      assertThat(first.id).isEqualTo(1);
      assertThat(first.name).isEqualTo("one");

      try (stream) {
        // more typically process with forEach ...
        stream.forEach(simpleData -> {
          System.out.println("process " + simpleData.id + " " + simpleData.name);
        });
      }
    });

    // wait ...
    future.get();
  }

  @Test
  void callStreamAsync() throws ExecutionException, InterruptedException {

    final CompletableFuture<Stream<SimpleData>> future = clientContext.request()
      .path("hello").path("stream")
      .GET().call()
      .stream(SimpleData.class).async();

    future.whenComplete((stream, throwable) -> {
      assertThat(throwable).isNull();
      final List<SimpleData> data = stream.collect(Collectors.toList());
      assertThat(data).hasSize(4);
    });
    // wait ...
    future.get();
  }

  @Test
  void callStream() {
    final Stream<SimpleData> stream = clientContext.request()
      .path("hello").path("stream")
      .GET().call()
      .stream(SimpleData.class).execute();

    final List<SimpleData> data = stream.collect(Collectors.toList());
    assertThat(data).hasSize(4);
  }

  @Test
  void async_stream_fromLineSubscriber() throws ExecutionException, InterruptedException {

    AtomicReference<HttpResponse<Void>> hresRef = new AtomicReference<>();
    AtomicReference<Throwable> errRef = new AtomicReference<>();
    AtomicReference<Boolean> completeRef = new AtomicReference<>();
    AtomicReference<Boolean> onSubscribeRef = new AtomicReference<>();

    final List<String> lines = new ArrayList<>();

    final CompletableFuture<HttpResponse<Void>> future = clientContext.request()
      .path("hello/stream")
      .GET()
      .async().withHandler(HttpResponse.BodyHandlers.fromLineSubscriber(new Flow.Subscriber<>() {
        @Override
        public void onSubscribe(Flow.Subscription subscription) {
          subscription.request(Long.MAX_VALUE);
          onSubscribeRef.set(true);
        }

        @Override
        public void onNext(String item) {
          lines.add(item);
        }

        @Override
        public void onError(Throwable throwable) {
          errRef.set(throwable);
        }

        @Override
        public void onComplete() {
          completeRef.set(true);
        }
      })).whenComplete((hres, throwable) -> {
        hresRef.set(hres);
        assertThat(hres.statusCode()).isEqualTo(200);
        assertThat(throwable).isNull();
      });

    // just wait
    assertThat(future.get()).isSameAs(hresRef.get());

    assertThat(onSubscribeRef.get()).isTrue();
    assertThat(completeRef.get()).isTrue();
    assertThat(errRef.get()).isNull();
    assertThat(lines).hasSize(4);

    final String first = lines.get(0);
    assertThat(first).isEqualTo("{\"id\":1, \"name\":\"one\"}");
  }

  @Test
  void get_helloMessage() {

    final HttpResponse<String> hres = clientContext.request()
      .path("hello").path("message")
      .GET().asString();

    assertThat(hres.body()).contains("hello world");
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void get_notFound() {
    UUID nullUUID = null;
    final HttpClientRequest request = clientContext.request()
      .path("hello").path(UUID.randomUUID()).queryParam("zone", ZoneId.of("UTC"))
      .header("X-Zone", ZoneId.of("UTC"))
      .header("X-Foo", nullUUID);

    assertThat(request.header("X-Zone")).containsExactly("UTC");
    assertThat(request.header("X-Foo")).isEmpty();

    final HttpResponse<String> hres = request.GET().asString();

    assertThat(hres.statusCode()).isEqualTo(404);
    assertThat(hres.body()).contains("Not found");
  }

  @Test
  void headers() {
    final HttpClientRequest request = clientContext.request();

    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("A", "a");
    headers.put("B", "b");
    headers.put("C", "c");
    request.header(headers);
    request.header("B", "b2");

    assertThat(request.header("A")).containsExactly("a");
    assertThat(request.header("B")).containsExactly("b", "b2");
    assertThat(request.header("C")).containsExactly("c");
  }

  @Test
  void callString() {
    final HttpResponse<String> hres = clientContext.request()
      .path("hello").path("message")
      .GET().call().asString().execute();

    assertThat(hres.body()).contains("hello world");
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void callStringAsync() throws ExecutionException, InterruptedException {
    final HttpResponse<String> hres = clientContext.request()
      .path("hello").path("message")
      .GET().call().asString().async().get();

    assertThat(hres.body()).contains("hello world");
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void callWithHandler() {
    final HttpResponse<String> hres = clientContext.request()
      .path("hello").path("message")
      .GET().call().withHandler(HttpResponse.BodyHandlers.ofString())
      .execute();

    assertThat(hres.body()).contains("hello world");
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void callWithHandlerAsync() throws ExecutionException, InterruptedException {
    final HttpResponse<String> hres = clientContext.request()
      .path("hello").path("message")
      .GET().call().withHandler(HttpResponse.BodyHandlers.ofString())
      .async().get();

    assertThat(hres.body()).contains("hello world");
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void async_get_asString() throws ExecutionException, InterruptedException {

    AtomicReference<HttpResponse<String>> ref = new AtomicReference<>();

    final CompletableFuture<HttpResponse<String>> future = clientContext.request()
      .path("hello").path("message")
      .GET()
      .async().asString()
      .whenComplete((hres, throwable) -> {
        ref.set(hres);
        assertThat(hres.statusCode()).isEqualTo(200);
        assertThat(hres.body()).contains("hello world");
      });

    final HttpResponse<String> hres = future.get();
    assertThat(hres).isSameAs(ref.get());
    assertThat(hres.body()).contains("hello world");
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void asyncViaCall_get_asString() throws ExecutionException, InterruptedException {
    AtomicReference<HttpResponse<String>> ref = new AtomicReference<>();
    final CompletableFuture<HttpResponse<String>> future = clientContext.request()
      .path("hello").path("message")
      .GET()
      .call().asString().async()
      .whenComplete((hres, throwable) -> {
        ref.set(hres);
        assertThat(hres.statusCode()).isEqualTo(200);
        assertThat(hres.body()).contains("hello world");
      });

    final HttpResponse<String> hres = future.get();
    assertThat(hres).isSameAs(ref.get());
  }

  @Test
  void async_get_asDiscarding() throws ExecutionException, InterruptedException {

    final CompletableFuture<HttpResponse<Void>> future = clientContext.request()
      .path("hello").path("message")
      .GET()
      .async().asDiscarding();

    final HttpResponse<Void> hres = future.get();
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void asyncViaCall_get_asDiscarding() throws ExecutionException, InterruptedException {
    final CompletableFuture<HttpResponse<Void>> future = clientContext.request()
      .path("hello").path("message")
      .GET()
      .call().asDiscarding().async();

    final HttpResponse<Void> hres = future.get();
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void get_helloMessage_via_url() {

    final HttpResponse<String> hres = clientContext.request()
      .url("http://127.0.0.1:8887")
      .path("hello").path("message")
      .GET().asString();

    assertThat(hres.body()).contains("hello world");
    assertThat(hres.statusCode()).isEqualTo(200);
  }

  @Test
  void get_hello_returningListOfBeans() {

    final List<HelloDto> helloDtos = clientContext.request()
      .path("hello")
      .GET().list(HelloDto.class);

    assertThat(helloDtos).hasSize(2);
  }

  @Test
  void callList() {
    final List<HelloDto> helloDtos = clientContext.request()
      .path("hello")
      .GET().call().list(HelloDto.class).execute();

    assertThat(helloDtos).hasSize(2);
  }

  @Test
  void callListAsync() throws ExecutionException, InterruptedException {
    final List<HelloDto> helloDtos = clientContext.request()
      .path("hello")
      .GET().call().list(HelloDto.class).async().get();

    assertThat(helloDtos).hasSize(2);
  }

  @Test
  void async_list() throws ExecutionException, InterruptedException {

    AtomicReference<List<HelloDto>> ref = new AtomicReference<>();

    final CompletableFuture<List<HelloDto>> future = clientContext.request()
      .path("hello")
      .GET().async()
      .list(HelloDto.class);

    future.whenComplete((helloDtos, throwable) -> {
      assertThat(throwable).isNull();
      assertThat(helloDtos).hasSize(2);
      ref.set(helloDtos);
    });

    final List<HelloDto> helloDtos = future.get();
    assertThat(helloDtos).hasSize(2);
    assertThat(helloDtos).isSameAs(ref.get());
  }

  @Test
  void get_withPathParamAndQueryParam_returningBean() {

    final HelloDto dto = clientContext.request()
      .path("hello/43/2020-03-05").queryParam("otherParam", "other").queryParam("foo", null)
      .GET()
      .bean(HelloDto.class);

    assertThat(dto.id).isEqualTo(43L);
    assertThat(dto.name).isEqualTo("2020-03-05");
    assertThat(dto.otherParam).isEqualTo("other");
  }

  @Test
  void callBean() {
    final HelloDto dto = clientContext.request()
      .path("hello/43/2020-03-05").queryParam("otherParam", "other").queryParam("foo", null)
      .GET()
      .call().bean(HelloDto.class).execute();

    assertThat(dto.id).isEqualTo(43L);
    assertThat(dto.name).isEqualTo("2020-03-05");
    assertThat(dto.otherParam).isEqualTo("other");
  }

  @Test
  void callBeanAsync() throws ExecutionException, InterruptedException {
    final CompletableFuture<HelloDto> future = clientContext.request()
      .path("hello/43/2020-03-05").queryParam("otherParam", "other").queryParam("foo", null)
      .GET()
      .call().bean(HelloDto.class).async();

    final HelloDto dto = future.get();
    assertThat(dto.id).isEqualTo(43L);
    assertThat(dto.name).isEqualTo("2020-03-05");
    assertThat(dto.otherParam).isEqualTo("other");
  }

  @Test
  void async_whenComplete_returningBean() throws ExecutionException, InterruptedException {

    final AtomicInteger counter = new AtomicInteger();
    final AtomicReference<HelloDto> ref = new AtomicReference<>();

    final CompletableFuture<HelloDto> future = clientContext.request()
      .path("hello/43/2020-03-05").queryParam("otherParam", "other").queryParam("foo", null)
      .GET()
      .async().bean(HelloDto.class);

    future.whenComplete((dto, throwable) -> {
      counter.incrementAndGet();
      ref.set(dto);

      assertThat(throwable).isNull();
      assertThat(dto.id).isEqualTo(43L);
      assertThat(dto.name).isEqualTo("2020-03-05");
      assertThat(dto.otherParam).isEqualTo("other");
    });

    // wait ...
    final HelloDto dto = future.get();
    assertThat(counter.incrementAndGet()).isEqualTo(2);
    assertThat(dto).isSameAs(ref.get());

    assertThat(dto.id).isEqualTo(43L);
    assertThat(dto.name).isEqualTo("2020-03-05");
    assertThat(dto.otherParam).isEqualTo("other");
  }

  @Test
  void async_whenComplete_throwingHttpException() {

    AtomicReference<HttpException> causeRef = new AtomicReference<>();

    final CompletableFuture<HelloDto> future = clientContext.request()
      .path("hello/saveform3")
      .formParam("name", "Bax")
      .formParam("email", "notValidEmail")
      .formParam("url", "notValidUrl")
      .formParam("startDate", "2030-12-03")
      .POST()
      .async()
      .bean(HelloDto.class)
      .whenComplete((helloDto, throwable) -> {
        // we get a throwable
        assertThat(throwable.getCause()).isInstanceOf(HttpException.class);
        assertThat(helloDto).isNull();

        final HttpException httpException = (HttpException) throwable.getCause();
        causeRef.set(httpException);
        assertThat(httpException.getStatusCode()).isEqualTo(422);

        // convert json error response body to a bean
        final ErrorResponse errorResponse = httpException.bean(ErrorResponse.class);

        final Map<String, String> errorMap = errorResponse.getErrors();
        assertThat(errorMap.get("url")).isEqualTo("must be a valid URL");
        assertThat(errorMap.get("email")).isEqualTo("must be a well-formed email address");
      });

    try {
      future.join();
    } catch (CompletionException e) {
      assertThat(e.getCause()).isSameAs(causeRef.get());
    }
  }

  @Test
  void async_exceptionally_style() {

    AtomicReference<HttpException> causeRef = new AtomicReference<>();

    final CompletableFuture<HelloDto> future = clientContext.request()
      .path("hello/saveform3")
      .formParam("name", "Bax")
      .formParam("email", "notValidEmail")
      .formParam("url", "notValidUrl")
      .formParam("startDate", "2030-12-03")
      .POST()
      .async()
      .bean(HelloDto.class);

    future.exceptionally(throwable -> {
      final HttpException httpException = (HttpException) throwable.getCause();
      causeRef.set(httpException);
      assertThat(httpException.statusCode()).isEqualTo(422);

      return new HelloDto(0, "ErrorResponse", "");

    }).thenAccept(helloDto -> {
      assertThat(helloDto.name).isEqualTo("ErrorResponse");
    });

    try {
      future.join();
    } catch (CompletionException e) {
      assertThat(e.getCause()).isSameAs(causeRef.get());
    }
  }

  @Test
  void post_bean_returningBean_usingExplicitConverters() {

    HelloDto dto = new HelloDto(12, "rob", "other");

    final BodyWriter from = clientContext.converters().beanWriter(HelloDto.class);
    final BodyReader<HelloDto> toDto = clientContext.converters().beanReader(HelloDto.class);

    final HelloDto bean = clientContext.request()
      .path("hello")
      .body(from.write(dto))
      .POST()
      .read(toDto);

    assertEquals("posted", bean.name);
    assertEquals(12, bean.id);
  }

  @Test
  void post_bean_returningVoid() {

    HelloDto dto = new HelloDto(12, "rob", "other");

    final HttpResponse<Void> res = clientContext.request()
      .path("hello/savebean/foo")
      .body(dto)
      .POST()
      .asDiscarding();

    assertThat(res.statusCode()).isEqualTo(201);
  }

  @Test
  void postForm() {

    UUID nullUUID = null;

    final HttpResponse<Void> res = clientContext.request()
      .path("hello/saveform")
      .formParam("name", "Bazz")
      .formParam("email", "user@foo.com")
      .formParam("url", "http://foo.com")
      .formParam("startDate", LocalDate.of(2030, 12, 03))
      .formParam("setToNull", null)
      .formParam("nullUid", nullUUID)
      .POST()
      .asDiscarding();

    assertThat(res.statusCode()).isEqualTo(201);
  }

  @Test
  void postForm_asMap() {
    Map<String, String> formParams = new LinkedHashMap<>();
    formParams.put("name", "Bazz");
    formParams.put("email", "user@foo.com");
    formParams.put("url", "http://foo.com");
    formParams.put("startDate", LocalDate.of(2030, 12, 03).toString());

    final HttpResponse<Void> res = clientContext.request()
      .path("hello/saveform")
      .formParam(formParams)
      .POST()
      .asDiscarding();

    assertThat(res.statusCode()).isEqualTo(201);
  }

  @Test
  void postForm_returningBean() {

    final HttpResponse<Void> res = clientContext.request()
      .path("hello/saveform")
      .formParam("name", "Bazz")
      .formParam("email", "user@foo.com")
      .formParam("url", "http://foo.com")
      .formParam("startDate", "2030-12-03")
      .POST()
      .asDiscarding();

    assertThat(res.statusCode()).isEqualTo(201);

    final HelloDto bean = clientContext.request()
      .path("hello/saveform3")
      .formParam("name", "Bax")
      .formParam("email", "Bax@foo.com")
      .formParam("url", "http://foo.com")
      .formParam("startDate", "2030-12-03")
      .POST()
      .bean(HelloDto.class);

    assertThat(bean.name).isEqualTo("Bax");
    assertThat(bean.otherParam).isEqualTo("Bax@foo.com");
    assertThat(bean.id).isEqualTo(52);
  }

  @Test
  void postForm_asVoid_validResponse() {
    HttpResponse<Void> res = clientContext.request()
      .path("hello/saveform")
      .formParam("name", "baz")
      .formParam("email", "user@foo.com")
      .formParam("url", "http://foo")
      .POST()
      .asVoid();

    assertEquals(201, res.statusCode());
    assertThat(res.headers().map()).isNotEmpty();
    assertThat(res.body()).isNull();
    assertThat(res.request()).isNotNull();
    assertThat(res.previousResponse()).isEmpty();
    assertThat(res.sslSession()).isEmpty();
    assertThat(res.version()).isEqualTo(HttpClient.Version.HTTP_1_1);
    assertThat(res.uri().toString()).isEqualTo("http://localhost:8887/hello/saveform");
  }

  @Test
  void postForm_asVoid_invokesValidation_expect_badRequest_extractError() {
    try {
      clientContext.request()
        .path("hello/saveform")
        .formParam("email", "user@foo.com")
        .formParam("url", "notAValidUrl")
        .POST()
        .asVoid();

      fail();

    } catch (HttpException e) {
      assertEquals(422, e.statusCode());

      final HttpResponse<?> httpResponse = e.getHttpResponse();
      assertNotNull(httpResponse);
      assertEquals(422, httpResponse.statusCode());

      final ErrorResponse errorResponse = e.bean(ErrorResponse.class);

      final Map<String, String> errorMap = errorResponse.getErrors();
      assertThat(errorMap.get("url")).isEqualTo("must be a valid URL");
      assertThat(errorMap.get("name")).isEqualTo("must not be null");
    }
  }

  @Test
  void asyncAsVoid_extractError() throws InterruptedException {
    AtomicReference<HttpException> ref = new AtomicReference<>();

    final CompletableFuture<HttpResponse<Void>> future =
      clientContext.request()
        .path("hello/saveform")
        .formParam("email", "user@foo.com")
        .formParam("url", "notAValidUrl")
        .POST()
        .async()
        .asVoid()
        .whenComplete((hres, throwable) -> {

          final HttpException cause = (HttpException) throwable.getCause();
          ref.set(cause);

          final HttpResponse<?> httpResponse = cause.httpResponse();
          assertNotNull(httpResponse);
          assertEquals(422, httpResponse.statusCode());

          final ErrorResponse errorResponse = cause.bean(ErrorResponse.class);
          final Map<String, String> errorMap = errorResponse.getErrors();
          assertThat(errorMap.get("url")).isEqualTo("must be a valid URL");
          assertThat(errorMap.get("name")).isEqualTo("must not be null");
        });

    try {
      future.get();
    } catch (ExecutionException e) {
      assertThat(ref.get()).isNotNull();
    }
  }

  @Test
  void callAsVoid_async_extractError() throws InterruptedException {
    AtomicReference<HttpException> ref = new AtomicReference<>();

    final CompletableFuture<HttpResponse<Void>> future =
      clientContext.request()
        .path("hello/saveform")
        .formParam("email", "user@foo.com")
        .formParam("url", "notAValidUrl")
        .POST()
        .call()
        .asVoid()
        .async()
        .whenComplete((hres, throwable) -> {

          final HttpException cause = (HttpException) throwable.getCause();
          ref.set(cause);

          final HttpResponse<?> httpResponse = cause.httpResponse();
          assertNotNull(httpResponse);
          assertEquals(422, httpResponse.statusCode());

          final ErrorResponse errorResponse = cause.bean(ErrorResponse.class);
          final Map<String, String> errorMap = errorResponse.getErrors();
          assertThat(errorMap.get("url")).isEqualTo("must be a valid URL");
          assertThat(errorMap.get("name")).isEqualTo("must not be null");
        });

    try {
      future.get();
    } catch (ExecutionException e) {
      assertThat(ref.get()).isNotNull();
    }
  }

  @Test
  void callAsVoid_extractError() {
    try {
      clientContext.request()
        .path("hello/saveform")
        .formParam("email", "user@foo.com")
        .formParam("url", "notAValidUrl")
        .POST()
        .call()
        .asVoid()
        .execute();

      fail();
    } catch (HttpException e) {
      final HttpResponse<?> httpResponse = e.httpResponse();
      assertNotNull(httpResponse);
      assertEquals(422, httpResponse.statusCode());

      final ErrorResponse errorResponse = e.bean(ErrorResponse.class);
      final Map<String, String> errorMap = errorResponse.getErrors();
      assertThat(errorMap.get("url")).isEqualTo("must be a valid URL");
      assertThat(errorMap.get("name")).isEqualTo("must not be null");
    }
  }

  @Test
  void postForm_asBytes_validation_expect_badRequest_extractError() {
    try {
      clientContext.request()
        .path("hello/saveform")
        .formParam("email", "user@foo.com")
        .formParam("url", "notAValidUrl")
        .POST().asVoid();

      fail();

    } catch (HttpException e) {
      assertEquals(422, e.statusCode());

      final HttpResponse<?> httpResponse = e.httpResponse();
      assertNotNull(httpResponse);
      assertEquals(422, httpResponse.statusCode());

      final ErrorResponse errorResponse = e.bean(ErrorResponse.class);
      final Map<String, String> errorMap = errorResponse.getErrors();
      assertThat(errorMap.get("url")).isEqualTo("must be a valid URL");
      assertThat(errorMap.get("name")).isEqualTo("must not be null");

      String rawBody = e.bodyAsString();
      assertThat(rawBody).contains("must be a valid URL");

      final byte[] rawBytes = e.bodyAsBytes();
      assertThat(rawBytes).isNotNull();
    }
  }

  @Test
  void delete() {
    final HttpResponse<Void> res =
      clientContext.request()
        .path("hello/52")
        .DELETE()
        .asDiscarding();

    assertThat(res.statusCode()).isEqualTo(204);
  }

  @Test
  void callAsVoid() {
    final HttpResponse<Void> res =
      clientContext.request()
        .path("hello/52")
        .DELETE()
        .call().asVoid().execute();

    assertThat(res.statusCode()).isEqualTo(204);
  }

  @Test
  void callAsVoid_async() throws ExecutionException, InterruptedException {
    final HttpResponse<Void> res =
      clientContext.request()
        .path("hello/52")
        .DELETE()
        .call().asVoid().async().get();

    assertThat(res.statusCode()).isEqualTo(204);
  }

  @Test
  void callAsDiscarding() {
    final HttpResponse<Void> res =
      clientContext.request()
        .path("hello").path(52)
        .DELETE()
        .call().asDiscarding().execute();

    assertThat(res.statusCode()).isEqualTo(204);
  }

  @Test
  void callAsDiscardingAsync() throws ExecutionException, InterruptedException {
    final CompletableFuture<HttpResponse<Void>> future = clientContext.request()
      .path("hello").path(52L)
      .DELETE()
      .call().asDiscarding().async();

    final HttpResponse<Void> res = future.get();
    assertThat(res.statusCode()).isEqualTo(204);
  }

  @Test
  void get_withMatrixParam() {
    final HttpResponse<String> httpRes = clientContext.request()
      .requestTimeout(Duration.ofSeconds(40))
      .path("hello/withMatrix/2011")
      .matrixParam("author", "rob")
      .matrixParam("country", "nz")
      .matrixParam("zone", ZoneId.of("UTC"))
      .path("foo")
      .queryParam("extra", "banana")
      .matrixParam("setToNull", null)
      .GET()
      .asString();

    assertEquals(200, httpRes.statusCode());
    assertEquals("yr:2011 au:rob co:nz zone:UTC other:foo extra:banana", httpRes.body());
  }

  public static class SimpleData {
    public long id;
    public String name;
  }
}
