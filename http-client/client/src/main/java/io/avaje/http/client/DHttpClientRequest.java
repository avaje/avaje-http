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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.net.http.HttpResponse.BodyHandlers.discarding;

class DHttpClientRequest implements HttpClientRequest, HttpClientResponse {

  private static final String CONTENT_TYPE = "Content-Type";
  private static final String CONTENT_ENCODING = "Content-Encoding";

  private final DHttpClientContext context;

  private final UrlBuilder url;

  private Duration requestTimeout;

  private boolean gzip;

  private BodyContent encodedRequestBody;

  private HttpRequest.BodyPublisher body;

  private HttpRequest.Builder httpRequest;

  private Map<String, String> formParams;

  private Map<String, String> headers;

  private boolean bodyFormEncoded;

  private long requestTimeNanos;

  private HttpResponse<?> httpResponse;

  private BodyContent encodedResponseBody;

  public DHttpClientRequest(DHttpClientContext context, Duration requestTimeout) {
    this.context = context;
    this.requestTimeout = requestTimeout;
    this.url = context.url();
  }

  @Override
  public HttpClientRequest requestTimeout(Duration requestTimeout) {
    this.requestTimeout = requestTimeout;
    return this;
  }

  @Override
  public HttpClientRequest header(String name, String value) {
    if (headers == null) {
      headers = new LinkedHashMap<>();
    }
    headers.put(name, value);
    return this;
  }

  @Override
  public HttpClientRequest gzip(boolean gzip) {
    this.gzip = gzip;
    return this;
  }

  @Override
  public HttpClientRequest path(String path) {
    url.path(path);
    return this;
  }

  @Override
  public HttpClientRequest matrixParam(String name, String value) {
    url.matrixParam(name, value);
    return this;
  }


  @Override
  public HttpClientRequest param(String name, String value) {
    url.param(name, value);
    return this;
  }

  @Override
  public HttpClientRequest formParam(String name, String value) {
    if (formParams == null) {
      formParams = new LinkedHashMap<>();
    }
    formParams.put(name, value);
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
  public HttpClientRequest body(Path file) throws FileNotFoundException {
    this.body = HttpRequest.BodyPublishers.ofFile(file);
    return this;
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
      return null;
    }
  }

  private HttpRequest.BodyPublisher bodyFromForm() {
    final String content = buildEncodedFormContent();
    bodyFormEncoded = true;
    return HttpRequest.BodyPublishers.ofString(content);
  }

  private String buildEncodedFormContent() {
    var builder = new StringBuilder(80);
    for (Map.Entry<String, String> entry : formParams.entrySet()) {
      if (builder.length() > 0) {
        builder.append("&");
      }
      builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
      builder.append("=");
      builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
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
      for (Map.Entry<String, String> header : headers.entrySet()) {
        httpRequest.header(header.getKey(), header.getValue());
      }
    }
  }

  public HttpClientResponse get() {
    httpRequest = newGet(url.build());
    addHeaders();
    return this;
  }

  @Override
  public HttpClientResponse delete() {
    httpRequest = newDelete(url.build());
    addHeaders();
    return this;
  }

  @Override
  public HttpClientResponse post() {
    httpRequest = newPost(url.build(), body());
    addHeaders();
    return this;
  }

  @Override
  public HttpClientResponse put() {
    httpRequest = newPut(url.build(), body());
    addHeaders();
    return this;
  }

  private void readResponseContent() {
    final HttpResponse<byte[]> response = asByteArray();
    this.httpResponse = response;
    context.check(response);
    encodedResponseBody = context.readContent(response);
    context.afterResponse(this);
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
  public <T> HttpResponse<T> withResponseHandler(HttpResponse.BodyHandler<T> responseHandler) {
    long startNanos = System.nanoTime();
    final HttpResponse<T> response = context.send(httpRequest, responseHandler);
    this.requestTimeNanos = System.nanoTime() - startNanos;
    this.httpResponse = response;
    context.afterResponseHandler(this);
    return response;
  }

  @Override
  public HttpResponse<byte[]> asByteArray() {
    return withResponseHandler(HttpResponse.BodyHandlers.ofByteArray());
  }

  @Override
  public HttpResponse<String> asString() {
    return withResponseHandler(HttpResponse.BodyHandlers.ofString());
  }

  @Override
  public HttpResponse<Void> asDiscarding() {
    return withResponseHandler(discarding());
  }

  @Override
  public HttpResponse<InputStream> asInputStream() {
    return withResponseHandler(HttpResponse.BodyHandlers.ofInputStream());
  }

  @Override
  public HttpResponse<Path> asFile(Path file) {
    return withResponseHandler(HttpResponse.BodyHandlers.ofFile(file));
  }

  @Override
  public HttpResponse<Stream<String>> asLines() {
    return withResponseHandler(HttpResponse.BodyHandlers.ofLines());
  }

  protected HttpRequest.Builder newGet(String url) {
    return HttpRequest.newBuilder()
      .uri(URI.create(url))
      .timeout(requestTimeout)
      .GET();
  }

  protected HttpRequest.Builder newDelete(String url) {
    return HttpRequest.newBuilder()
      .uri(URI.create(url))
      .timeout(requestTimeout)
      .DELETE();
  }

  public HttpRequest.Builder newPost(String url, HttpRequest.BodyPublisher body) {
    return newRequest("POST", url, body);
  }

  public HttpRequest.Builder newPut(String url, HttpRequest.BodyPublisher body) {
    return newRequest("PUT", url, body);
  }

  public HttpRequest.Builder newPatch(String url, HttpRequest.BodyPublisher body) {
    return newRequest("PATCH", url, body);
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

  ResponseListener.Event listenerEvent() {
    return new ListenerEvent();
  }

  private class ListenerEvent implements ResponseListener.Event {

    @Override
    public long responseTimeNanos() {
      return requestTimeNanos;
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
      if (encodedRequestBody != null) {
        return new String(encodedRequestBody.content(), StandardCharsets.UTF_8);
      }
      if (bodyFormEncoded) {
        return buildEncodedFormContent();
      }
      if (body != null) {
        return body.toString();
      }
      return null;
    }

    @Override
    public String responseBody() {
      if (encodedResponseBody != null) {
        return new String(encodedResponseBody.content(), StandardCharsets.UTF_8);
      }
      return null;
    }
  }

  static class HttpVoidResponse implements HttpResponse<Void> {

    private final HttpResponse<?> orig;

    @SuppressWarnings({"unchecked", "raw"})
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
