package io.avaje.http.client;

import javax.net.ssl.SSLSession;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.net.http.HttpResponse.BodyHandlers.discarding;

class DHttpClientRequest implements HttpClientRequest, HttpClientResponse {

  private static final String CONTENT_TYPE = "Content-Type";
  private static final String CONTENT_ENCODING = "Content-Encoding";
  private static final String VERB_DELETE = "DELETE";
  private static final String VERB_HEAD = "HEAD";
  private static final String VERB_PATCH = "PATCH";
  private static final String VERB_TRACE = "TRACE";

  private final DHttpClientContext context;
  private final UrlBuilder url;
  private Duration requestTimeout;
  private boolean gzip;

  private BodyContent encodedRequestBody;
  private HttpRequest.BodyPublisher body;
  private String rawRequestBody;

  private HttpRequest.Builder httpRequest;

  private Map<String, List<String>> formParams;
  private Map<String, List<String>> headers;

  private boolean bodyFormEncoded;
  private long responseTimeNanos;

  private HttpResponse<?> httpResponse;
  private BodyContent encodedResponseBody;
  private boolean loggableResponseBody;
  private boolean skipAuthToken;
  private boolean suppressLogging;
  protected long startAsyncNanos;
  private String label;
  private Map<String, Object> customAttributes;

  DHttpClientRequest(DHttpClientContext context, Duration requestTimeout) {
    this.context = context;
    this.requestTimeout = requestTimeout;
    this.url = context.url();
  }

  @Override
  public HttpClientRequest skipAuthToken() {
    this.skipAuthToken = true;
    this.suppressLogging = true;
    return this;
  }

  @Override
  public HttpClientRequest suppressLogging() {
    this.suppressLogging = true;
    return this;
  }

  @Override
  public HttpClientRequest label(String label) {
    this.label = label;
    return this;
  }

  @Override
  public String label() {
    return label;
  }

  @Override
  public HttpClientRequest setAttribute(String key, Object value) {
    if (customAttributes == null) {
      customAttributes = new HashMap<>();
    }
    customAttributes.put(key, value);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <E> E getAttribute(String key) {
    return customAttributes == null ? null : (E) customAttributes.get(key);
  }

  @Override
  public HttpClientRequest requestTimeout(Duration requestTimeout) {
    this.requestTimeout = requestTimeout;
    return this;
  }

  @Override
  public HttpClientRequest headerAddIfAbsent(String name, Object value) {
    if (headers == null || !headers.containsKey(name)) {
      header(name, value);
    }
    return this;
  }

  @Override
  public HttpClientRequest header(String name, String value) {
    if (headers == null) {
      headers = new LinkedHashMap<>();
    }
    headers.computeIfAbsent(name, s -> new ArrayList<>()).add(value);
    return this;
  }

  @Override
  public HttpClientRequest header(String name, Object value) {
    return value != null ? header(name, value.toString()) : this;
  }

  @Override
  public HttpClientRequest header(Map<String, ?> headers) {
    if (headers != null) {
      for (Map.Entry<String, ?> entry : headers.entrySet()) {
        header(entry.getKey(), entry.getValue());
      }
    }
    return this;
  }

  @Override
  public List<String> header(String name) {
    if (headers == null) {
      return Collections.emptyList();
    }
    final List<String> values = headers.get(name);
    return values == null ? Collections.emptyList() : values;
  }

  @Override
  public HttpClientRequest gzip(boolean gzip) {
    this.gzip = gzip;
    return this;
  }

  @Override
  public HttpClientRequest url(String baseUrl) {
    url.url(baseUrl);
    return this;
  }

  @Override
  public HttpClientRequest path(String path) {
    url.path(path);
    return this;
  }

  @Override
  public HttpClientRequest path(int val) {
    url.path(val);
    return this;
  }

  @Override
  public HttpClientRequest path(long val) {
    url.path(val);
    return this;
  }

  @Override
  public HttpClientRequest path(Object val) {
    url.path(val);
    return this;
  }

  @Override
  public HttpClientRequest matrixParam(String name, String value) {
    url.matrixParam(name, value);
    return this;
  }

  @Override
  public HttpClientRequest matrixParam(String name, Object value) {
    url.matrixParam(name, value);
    return this;
  }

  @Override
  public HttpClientRequest queryParam(String name, String value) {
    url.queryParam(name, value);
    return this;
  }

  @Override
  public HttpClientRequest queryParam(String name, Object value) {
    url.queryParam(name, value);
    return this;
  }

  @Override
  public HttpClientRequest queryParam(Map<String, ?> params) {
    url.queryParam(params);
    return this;
  }

  @Override
  public HttpClientRequest formParam(String name, String value) {
    if (value == null) {
      return this;
    }
    if (formParams == null) {
      formParams = new LinkedHashMap<>();
    }
    formParams.computeIfAbsent(name, s -> new ArrayList<>()).add(value);
    return this;
  }

  @Override
  public HttpClientRequest formParam(String name, Object value) {
    return value != null ? formParam(name, value.toString()) : this;
  }

  @Override
  public HttpClientRequest formParam(Map<String, ?> params) {
    if (params != null) {
      for (Map.Entry<String, ?> entry : params.entrySet()) {
        formParam(entry.getKey(), entry.getValue());
      }
    }
    return this;
  }

  @Override
  public HttpClientRequest body(BodyContent bodyContent) {
    encodedRequestBody = bodyContent;
    return this;
  }

  @Override
  public HttpClientRequest body(Object bean, String contentType) {
    encodedRequestBody = context.write(bean, contentType);
    return this;
  }

  @Override
  public HttpClientRequest body(Object bean) {
    return body(bean, null);
  }

  @Override
  public HttpClientRequest body(String body) {
    this.rawRequestBody = body;
    this.body = HttpRequest.BodyPublishers.ofString(body);
    return this;
  }

  @Override
  public HttpClientRequest body(byte[] bytes) {
    this.body = HttpRequest.BodyPublishers.ofByteArray(bytes);
    return this;
  }

  @Override
  public HttpClientRequest body(Supplier<? extends InputStream> streamSupplier) {
    this.body = HttpRequest.BodyPublishers.ofInputStream(streamSupplier);
    return this;
  }

  @Override
  public HttpClientRequest body(Path file) {
    try {
      this.body = HttpRequest.BodyPublishers.ofFile(file);
      return this;
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("File not found " + file, e);
    }
  }

  @Override
  public HttpClientRequest body(HttpRequest.BodyPublisher body) {
    this.body = body;
    return this;
  }

  private HttpRequest.BodyPublisher body() {
    if (body != null) {
      return body;
    } else if (encodedRequestBody != null) {
      return fromEncodedBody();
    } else if (formParams != null) {
      return bodyFromForm();
    } else {
      return HttpRequest.BodyPublishers.noBody();
    }
  }

  private HttpRequest.BodyPublisher bodyFromForm() {
    final String content = buildEncodedFormContent();
    bodyFormEncoded = true;
    return HttpRequest.BodyPublishers.ofString(content);
  }

  private String buildEncodedFormContent() {
    var builder = new StringBuilder(80);
    for (Map.Entry<String, List<String>> entry : formParams.entrySet()) {
      for (String value : entry.getValue()) {
        if (builder.length() > 0) {
          builder.append("&");
        }
        builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
        builder.append("=");
        builder.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
      }
    }
    return builder.toString();
  }

  private HttpRequest.BodyPublisher fromEncodedBody() {
    if (gzip) {
      return HttpRequest.BodyPublishers.ofByteArray(GzipUtil.gzip(encodedRequestBody.content()));
    }
    return HttpRequest.BodyPublishers.ofByteArray(encodedRequestBody.content());
  }

  private void addHeaders() {
    if (encodedRequestBody != null) {
      final String contentType = encodedRequestBody.contentType();
      if (contentType != null) {
        httpRequest.header(CONTENT_TYPE, contentType);
      }
    } else if (bodyFormEncoded) {
      httpRequest.header(CONTENT_TYPE, "application/x-www-form-urlencoded");
    }
    if (gzip) {
      httpRequest.header(CONTENT_ENCODING, "gzip");
    }
    if (headers != null) {
      for (Map.Entry<String, List<String>> header : headers.entrySet()) {
        for (String value : header.getValue()) {
          httpRequest.header(header.getKey(), value);
        }
      }
    }
  }

  @Override
  public HttpAsyncResponse async() {
    return new DHttpAsync(this);
  }

  @Override
  public HttpCallResponse call() {
    return new DHttpCall(this);
  }

  @Override
  public HttpClientResponse HEAD() {
    httpRequest = newHead(url.build());
    return this;
  }

  public HttpClientResponse GET() {
    httpRequest = newGet(url.build());
    return this;
  }

  @Override
  public HttpClientResponse DELETE() {
    httpRequest = newDelete(url.build(), body());
    return this;
  }

  @Override
  public HttpClientResponse POST() {
    httpRequest = newPost(url.build(), body());
    return this;
  }

  @Override
  public HttpClientResponse PUT() {
    httpRequest = newPut(url.build(), body());
    return this;
  }

  @Override
  public HttpClientResponse PATCH() {
    httpRequest = newPatch(url.build(), body());
    return this;
  }

  @Override
  public HttpClientResponse TRACE() {
    httpRequest = newTrace(url.build(), body());
    return this;
  }

  private void readResponseContent() {
    final HttpResponse<byte[]> response = sendWith(HttpResponse.BodyHandlers.ofByteArray());
    encodedResponseBody = context.readContent(response);
    context.afterResponse(this);
    context.checkMaybeThrow(response);
  }

  @Override
  public HttpResponse<Void> asVoid() {
    readResponseContent();
    return new HttpVoidResponse(httpResponse);
  }

  @Override
  public <T> T read(BodyReader<T> reader) {
    readResponseContent();
    return reader.read(encodedResponseBody);
  }

  @Override
  public <T> T bean(Class<T> cls) {
    readResponseContent();
    return context.readBean(cls, encodedResponseBody);
  }

  @Override
  public <T> List<T> list(Class<T> cls) {
    readResponseContent();
    return context.readList(cls, encodedResponseBody);
  }

  @Override
  public <T> Stream<T> stream(Class<T> cls) {
    final HttpResponse<Stream<String>> res = withHandler(HttpResponse.BodyHandlers.ofLines());
    this.httpResponse = res;
    if (res.statusCode() >= 300) {
      throw new HttpException(res, context);
    }
    final BodyReader<T> bodyReader = context.beanReader(cls);
    return res.body().map(bodyReader::readBody);
  }

  @Override
  public <T> HttpResponse<T> withHandler(HttpResponse.BodyHandler<T> responseHandler) {
    final HttpResponse<T> response = sendWith(responseHandler);
    context.afterResponse(this);
    return response;
  }

  /**
   * Prepare and send the request but not performing afterResponse() handling.
   */
  private <T> HttpResponse<T> sendWith(HttpResponse.BodyHandler<T> responseHandler) {
    context.beforeRequest(this);
    addHeaders();
    HttpResponse<T> response = performSend(responseHandler);
    httpResponse = response;
    return response;
  }

  protected <T> HttpResponse<T> performSend(HttpResponse.BodyHandler<T> responseHandler) {
    final long startNanos = System.nanoTime();
    try {
      return context.send(httpRequest, responseHandler);
    } finally {
      responseTimeNanos = System.nanoTime() - startNanos;
    }
  }

  protected <T> CompletableFuture<HttpResponse<T>> performSendAsync(boolean loggable, HttpResponse.BodyHandler<T> responseHandler) {
    loggableResponseBody = loggable;
    context.beforeRequest(this);
    addHeaders();
    startAsyncNanos = System.nanoTime();
    return context.sendAsync(httpRequest, responseHandler);
  }

  protected HttpResponse<Void> asyncVoid(HttpResponse<byte[]> response) {
    afterAsyncEncoded(response);
    return new HttpVoidResponse(response);
  }

  protected <E> E asyncBean(Class<E> type, HttpResponse<byte[]> response) {
    afterAsyncEncoded(response);
    return context.readBean(type, encodedResponseBody);
  }

  protected <E> List<E> asyncList(Class<E> type, HttpResponse<byte[]> response) {
    afterAsyncEncoded(response);
    return context.readList(type, encodedResponseBody);
  }

  protected <E> Stream<E> asyncStream(Class<E> type, HttpResponse<Stream<String>> response) {
    responseTimeNanos = System.nanoTime() - startAsyncNanos;
    httpResponse = response;
    context.afterResponse(this);
    if (response.statusCode() >= 300) {
      throw new HttpException(response, context);
    }
    final BodyReader<E> bodyReader = context.beanReader(type);
    return response.body().map(bodyReader::readBody);
  }

  private void afterAsyncEncoded(HttpResponse<byte[]> response) {
    responseTimeNanos = System.nanoTime() - startAsyncNanos;
    httpResponse = response;
    encodedResponseBody = context.readContent(response);
    context.afterResponse(this);
    context.checkMaybeThrow(response);
  }

  protected <E> HttpResponse<E> afterAsync(HttpResponse<E> response) {
    responseTimeNanos = System.nanoTime() - startAsyncNanos;
    httpResponse = response;
    context.afterResponse(this);
    return response;
  }

  @Override
  public long responseTimeMicros() {
    return responseTimeNanos / 1000;
  }

  @Override
  public HttpResponse<byte[]> asByteArray() {
    return withHandler(HttpResponse.BodyHandlers.ofByteArray());
  }

  @Override
  public HttpResponse<String> asString() {
    loggableResponseBody = true;
    return withHandler(HttpResponse.BodyHandlers.ofString());
  }

  @Override
  public HttpResponse<String> asPlainString() {
    loggableResponseBody = true;
    final HttpResponse<String> hres = withHandler(HttpResponse.BodyHandlers.ofString());
    context.checkResponse(hres);
    return hres;
  }

  @Override
  public HttpResponse<Void> asDiscarding() {
    return withHandler(discarding());
  }

  @Override
  public HttpResponse<InputStream> asInputStream() {
    return withHandler(HttpResponse.BodyHandlers.ofInputStream());
  }

  @Override
  public HttpResponse<Path> asFile(Path file) {
    return withHandler(HttpResponse.BodyHandlers.ofFile(file));
  }

  @Override
  public HttpResponse<Stream<String>> asLines() {
    return withHandler(HttpResponse.BodyHandlers.ofLines());
  }

  private HttpRequest.Builder newReq(String url) {
    return HttpRequest.newBuilder()
      .uri(URI.create(url))
      .timeout(requestTimeout);
  }

  private HttpRequest.Builder newHead(String url) {
    return newRequest(VERB_HEAD, url, HttpRequest.BodyPublishers.noBody());
  }

  private HttpRequest.Builder newGet(String url) {
    return newReq(url).GET();
  }

  private HttpRequest.Builder newPost(String url, HttpRequest.BodyPublisher body) {
    return newReq(url).POST(body);
  }

  private HttpRequest.Builder newPut(String url, HttpRequest.BodyPublisher body) {
    return newReq(url).PUT(body);
  }

  private HttpRequest.Builder newPatch(String url, HttpRequest.BodyPublisher body) {
    return newRequest(VERB_PATCH, url, body);
  }

  private HttpRequest.Builder newDelete(String url, HttpRequest.BodyPublisher body) {
    // allow DELETE to have a body
    return newRequest(VERB_DELETE, url, body);
  }

  private HttpRequest.Builder newTrace(String url, HttpRequest.BodyPublisher body) {
    return newRequest(VERB_TRACE, url, body);
  }

  protected HttpRequest.Builder newRequest(String method, String url, HttpRequest.BodyPublisher body) {
    if (body == null) {
      throw new IllegalArgumentException("body is null but required for " + method + " to " + url);
    }
    return HttpRequest.newBuilder()
      .uri(URI.create(url))
      .timeout(requestTimeout)
      .method(method, body);
  }

  RequestListener.Event listenerEvent() {
    return new ListenerEvent();
  }

  HttpResponse<?> response() {
    return httpResponse;
  }

  boolean isSkipAuthToken() {
    return skipAuthToken;
  }

  private class ListenerEvent implements RequestListener.Event {

    @Override
    public long responseTimeMicros() {
      return responseTimeNanos / 1000;
    }

    @Override
    public URI uri() {
      return httpResponse.uri();
    }

    @Override
    public HttpResponse<?> response() {
      return httpResponse;
    }

    @Override
    public HttpRequest request() {
      return httpResponse.request();
    }

    @Override
    public String requestBody() {
      if (suppressLogging) {
        return "<suppressed request body>";
      }
      if (encodedRequestBody != null) {
        return new String(encodedRequestBody.content(), StandardCharsets.UTF_8);
      } else if (bodyFormEncoded) {
        return buildEncodedFormContent();
      } else if (rawRequestBody != null) {
        return rawRequestBody;
      } else if (body != null) {
        return body.toString();
      }
      return null;
    }

    @Override
    public String responseBody() {
      if (suppressLogging) {
        return "<suppressed response body>";
      }
      if (encodedResponseBody != null) {
        return context.maxResponseBody(new String(encodedResponseBody.content(), StandardCharsets.UTF_8));
      } else if (httpResponse != null && loggableResponseBody) {
        final Object body = httpResponse.body();
        return (body == null) ? null : context.maxResponseBody(body.toString());
      }
      return null;
    }
  }

  static class HttpVoidResponse implements HttpResponse<Void> {

    private final HttpResponse<?> orig;

    @SuppressWarnings({"raw"})
    HttpVoidResponse(HttpResponse<?> orig) {
      this.orig = orig;
    }

    @Override
    public int statusCode() {
      return orig.statusCode();
    }

    @Override
    public HttpRequest request() {
      return orig.request();
    }

    @Override
    public Optional<HttpResponse<Void>> previousResponse() {
      return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
      return orig.headers();
    }

    @Override
    public Void body() {
      return null;
    }

    @Override
    public Optional<SSLSession> sslSession() {
      return orig.sslSession();
    }

    @Override
    public URI uri() {
      return orig.uri();
    }

    @Override
    public HttpClient.Version version() {
      return orig.version();
    }
  }

}
