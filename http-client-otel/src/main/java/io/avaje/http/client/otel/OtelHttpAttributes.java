package io.avaje.http.client.otel;

import io.avaje.http.client.HttpClientRequest;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

final class OtelHttpAttributes {

  private static final String OTHER = "_OTHER";
  private static final AttributeKey<String> ERROR_TYPE = AttributeKey.stringKey("error.type");
  private static final AttributeKey<String> HTTP_REQUEST_METHOD = AttributeKey.stringKey("http.request.method");
  private static final AttributeKey<String> HTTP_REQUEST_METHOD_ORIGINAL = AttributeKey.stringKey("http.request.method_original");
  private static final AttributeKey<Long> HTTP_REQUEST_RESEND_COUNT = AttributeKey.longKey("http.request.resend_count");
  private static final AttributeKey<Long> HTTP_RESPONSE_STATUS_CODE = AttributeKey.longKey("http.response.status_code");
  private static final AttributeKey<String> NETWORK_PROTOCOL_NAME = AttributeKey.stringKey("network.protocol.name");
  private static final AttributeKey<String> NETWORK_PROTOCOL_VERSION = AttributeKey.stringKey("network.protocol.version");
  private static final AttributeKey<String> SERVER_ADDRESS = AttributeKey.stringKey("server.address");
  private static final AttributeKey<Long> SERVER_PORT = AttributeKey.longKey("server.port");
  private static final AttributeKey<String> URL_FULL = AttributeKey.stringKey("url.full");
  private static final AttributeKey<String> URL_TEMPLATE = AttributeKey.stringKey("url.template");

  private OtelHttpAttributes() {
  }

  static String spanName(HttpClientRequest request, OtelConfig config) {
    final String method = request.method();
    final String nameMethod = method == null || !config.knownMethods().contains(method) ? "HTTP" : method;
    final String template = urlTemplate(request, config);
    return template == null ? nameMethod : nameMethod + " " + template;
  }

  static void onStart(Span span, HttpClientRequest request, int resendCount, OtelConfig config) {
    final String method = request.method();
    if (method != null) {
      if (config.knownMethods().contains(method)) {
        span.setAttribute(HTTP_REQUEST_METHOD, method);
      } else {
        span.setAttribute(HTTP_REQUEST_METHOD, OTHER);
        span.setAttribute(HTTP_REQUEST_METHOD_ORIGINAL, method);
      }
    }

    if (resendCount > 0) {
      span.setAttribute(HTTP_REQUEST_RESEND_COUNT, (long) resendCount);
    }

    final String fullUrl = request.url();
    if (fullUrl != null) {
      span.setAttribute(URL_FULL, UrlSanitizer.sanitize(fullUrl, config.sensitiveQueryParameters()));
      addServerAttributes(span, fullUrl);
    }

    final String template = urlTemplate(request, config);
    if (template != null) {
      span.setAttribute(URL_TEMPLATE, template);
    }
  }

  static void captureRequestHeaders(Span span, HttpClientRequest request, OtelConfig config) {
    captureHeaders(span, "http.request.header.", request.headers(), config.capturedRequestHeaders());
  }

  static void onResponse(Span span, HttpResponse<?> response, OtelConfig config) {
    span.setAttribute(NETWORK_PROTOCOL_NAME, "http");
    span.setAttribute(HTTP_RESPONSE_STATUS_CODE, (long) response.statusCode());
    if (response.statusCode() >= 400) {
      span.setAttribute(ERROR_TYPE, Integer.toString(response.statusCode()));
      span.setStatus(StatusCode.ERROR);
    }

    final String protocolVersion = protocolVersion(response.version());
    if (protocolVersion != null) {
      span.setAttribute(NETWORK_PROTOCOL_VERSION, protocolVersion);
    }

    captureHeaders(span, "http.response.header.", response.headers().map(), config.capturedResponseHeaders());
  }

  static void onError(Span span, Throwable error) {
    Throwable actual = error;
    if (actual instanceof io.avaje.http.client.HttpException && actual.getCause() != null) {
      actual = actual.getCause();
    }
    if (actual instanceof CancellationException) {
      return;
    }
    span.setAttribute(ERROR_TYPE, actual.getClass().getName());
    span.recordException(actual);
    span.setStatus(StatusCode.ERROR);
  }

  private static void addServerAttributes(Span span, String rawUrl) {
    try {
      final URI uri = URI.create(rawUrl);
      if (uri.getHost() != null) {
        span.setAttribute(SERVER_ADDRESS, uri.getHost());
      }
      final int port = uri.getPort() == -1 ? defaultPort(uri.getScheme()) : uri.getPort();
      if (port > -1) {
        span.setAttribute(SERVER_PORT, (long) port);
      }
    } catch (IllegalArgumentException e) {
      // ignore invalid URLs
    }
  }

  private static void captureHeaders(
      Span span, String prefix, Map<String, List<String>> headers, List<String> capturedHeaders) {
    for (final String headerName : capturedHeaders) {
      final List<String> values = headerValues(headers, headerName);
      if (!values.isEmpty()) {
        span.setAttribute(AttributeKey.stringArrayKey(prefix + headerName), values);
      }
    }
  }

  private static List<String> headerValues(Map<String, List<String>> headers, String headerName) {
    for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
      if (entry.getKey().equalsIgnoreCase(headerName)) {
        return entry.getValue();
      }
    }
    return List.of();
  }

  private static int defaultPort(String scheme) {
    if ("http".equalsIgnoreCase(scheme)) {
      return 80;
    }
    if ("https".equalsIgnoreCase(scheme)) {
      return 443;
    }
    return -1;
  }

  private static String protocolVersion(HttpClient.Version version) {
    if (version == null) {
      return null;
    }
    switch (version) {
      case HTTP_1_1:
        return "1.1";
      case HTTP_2:
        return "2";
      default:
        return null;
    }
  }

  static String urlTemplate(HttpClientRequest request, OtelConfig config) {
    String template = null;
    if (config.urlTemplateResolver() != null) {
      template = config.urlTemplateResolver().apply(request);
    }
    if ((template == null || template.isBlank()) && config.useLabelAsUrlTemplate()) {
      template = request.label();
    }
    if (template == null || template.isBlank()) {
      return null;
    }
    return template;
  }
}
