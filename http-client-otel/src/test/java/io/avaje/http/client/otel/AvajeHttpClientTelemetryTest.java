package io.avaje.http.client.otel;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.avaje.http.client.HttpClient;
import io.avaje.http.client.SimpleRetryHandler;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AvajeHttpClientTelemetryTest {

  private final AtomicReference<String> traceparent = new AtomicReference<>();
  private final AtomicInteger retryCounter = new AtomicInteger();
  private HttpServer server;
  private SdkTracerProvider tracerProvider;

  @AfterEach
  void shutdown() {
    if (server != null) {
      server.stop(0);
    }
    if (tracerProvider != null) {
      tracerProvider.close();
    }
  }

  @Test
  void syncRequest_createsSpanCapturesHeadersAndPropagates() throws Exception {
    server = server();
    server.createContext("/hello", exchange -> {
      traceparent.set(exchange.getRequestHeaders().getFirst("traceparent"));
      exchange.getResponseHeaders().add("X-Reply", "reply-value");
      write(exchange, 200, "hello");
    });
    server.start();

    final InMemorySpanExporter exporter = InMemorySpanExporter.create();
    final OpenTelemetry openTelemetry = openTelemetry(exporter);
    final HttpClient client = AvajeHttpClientTelemetry.builder(openTelemetry)
      .capturedRequestHeaders(List.of("X-Test"))
      .capturedResponseHeaders(List.of("X-Reply"))
      .useLabelAsUrlTemplate(true)
      .build()
      .configure(HttpClient.builder().baseUrl(baseUrl()))
      .build();

    final HttpResponse<String> response = client.request()
      .path("hello")
      .label("/hello")
      .header("X-Test", "request-value")
      .GET()
      .asString();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(traceparent.get()).startsWith("00-");

    final List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(1);

    final SpanData span = spans.get(0);
    assertThat(span.getName()).isEqualTo("GET /hello");
    assertThat(span.getAttributes().get(AttributeKey.stringKey("http.request.method"))).isEqualTo("GET");
    assertThat(span.getAttributes().get(AttributeKey.stringKey("url.template"))).isEqualTo("/hello");
    assertThat(span.getAttributes().get(AttributeKey.longKey("http.response.status_code"))).isEqualTo(200L);
    assertThat(span.getAttributes().get(AttributeKey.stringArrayKey("http.request.header.x-test")))
      .containsExactly("request-value");
    assertThat(span.getAttributes().get(AttributeKey.stringArrayKey("http.response.header.x-reply")))
      .containsExactly("reply-value");
  }

  @Test
  void asyncRequest_usesCurrentSpanAsParent() throws Exception {
    server = server();
    server.createContext("/async", exchange -> write(exchange, 200, "async"));
    server.start();

    final InMemorySpanExporter exporter = InMemorySpanExporter.create();
    final OpenTelemetry openTelemetry = openTelemetry(exporter);
    final HttpClient client = AvajeHttpClientTelemetry.create(openTelemetry)
      .configure(HttpClient.builder().baseUrl(baseUrl()))
      .build();

    final Span parent = openTelemetry.getTracer("test").spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      client.request()
        .path("async")
        .GET()
        .async()
        .asString()
        .join();
    } finally {
      parent.end();
    }

    final List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(2);

    final SpanData child = spans.stream()
      .filter(span -> !"parent".equals(span.getName()))
      .findFirst()
      .orElseThrow(IllegalStateException::new);

    assertThat(child.getParentSpanContext().getSpanId()).isEqualTo(parent.getSpanContext().getSpanId());
  }

  @Test
  void retryRequest_createsSpanPerAttempt() throws Exception {
    server = server();
    server.createContext("/retry", exchange -> {
      final int attempt = retryCounter.incrementAndGet();
      final int status = attempt < 3 ? 500 : 200;
      write(exchange, status, attempt < 3 ? "retry" : "done");
    });
    server.start();

    final InMemorySpanExporter exporter = InMemorySpanExporter.create();
    final OpenTelemetry openTelemetry = openTelemetry(exporter);
    final HttpClient client = AvajeHttpClientTelemetry.builder(openTelemetry)
      .useLabelAsUrlTemplate(true)
      .build()
      .configure(HttpClient.builder()
        .baseUrl(baseUrl())
        .retryHandler(new SimpleRetryHandler(4, 1)))
      .build();

    final HttpResponse<String> response = client.request()
      .path("retry")
      .label("/retry")
      .GET()
      .asString();

    assertThat(response.body()).isEqualTo("done");

    final List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(3);
    assertThat(spans)
      .extracting(span -> span.getAttributes().get(AttributeKey.longKey("http.request.resend_count")))
      .containsExactly(null, 1L, 2L);
    assertThat(spans)
      .extracting(span -> span.getStatus().getStatusCode())
      .containsExactly(StatusCode.ERROR, StatusCode.ERROR, StatusCode.UNSET);
  }

  private OpenTelemetry openTelemetry(InMemorySpanExporter exporter) {
    tracerProvider = SdkTracerProvider.builder()
      .addSpanProcessor(SimpleSpanProcessor.create(exporter))
      .build();
    return OpenTelemetrySdk.builder()
      .setTracerProvider(tracerProvider)
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
      .build();
  }

  private HttpServer server() throws IOException {
    return HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
  }

  private String baseUrl() {
    return "http://127.0.0.1:" + server.getAddress().getPort();
  }

  private void write(HttpExchange exchange, int status, String body) throws IOException {
    final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(status, bytes.length);
    try (OutputStream outputStream = exchange.getResponseBody()) {
      outputStream.write(bytes);
    }
    exchange.close();
  }
}
