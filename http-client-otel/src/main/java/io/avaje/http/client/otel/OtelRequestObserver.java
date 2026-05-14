package io.avaje.http.client.otel;

import io.avaje.http.client.HttpClientRequest;
import io.avaje.http.client.RequestObserver;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class OtelRequestObserver implements RequestObserver {

  private static final String INSTRUMENTATION_NAME = "io.avaje.http.client.otel";

  private static final TextMapSetter<HttpClientRequest> REQUEST_HEADER_SETTER =
    new TextMapSetter<HttpClientRequest>() {
      @Override
      public void set(HttpClientRequest carrier, String key, String value) {
        final Map<String, List<String>> headers = carrier.headers();
        if (headers.isEmpty()) {
          carrier.header(key, value);
          return;
        }

        String existingKey = null;
        for (final String headerName : headers.keySet()) {
          if (headerName.equalsIgnoreCase(key)) {
            existingKey = headerName;
            break;
          }
        }
        headers.put(existingKey == null ? key : existingKey, new ArrayList<>(List.of(value)));
      }
    };

  private final Tracer tracer;
  private final io.opentelemetry.context.propagation.TextMapPropagator propagator;
  private final OtelConfig config;

  OtelRequestObserver(OpenTelemetry openTelemetry, OtelConfig config) {
    this.tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);
    this.propagator = openTelemetry.getPropagators().getTextMapPropagator();
    this.config = config;
  }

  @Override
  public Observation start(HttpClientRequest request) {
    return new OtelObservation(Context.current());
  }

  private final class OtelObservation implements Observation {

    private final Context parentContext;

    private OtelObservation(Context parentContext) {
      this.parentContext = parentContext;
    }

    @Override
    public Attempt startAttempt(HttpClientRequest request, int resendCount) {
      final Span span = tracer.spanBuilder(OtelHttpAttributes.spanName(request, config))
        .setSpanKind(SpanKind.CLIENT)
        .setParent(parentContext)
        .startSpan();

      final Context spanContext = parentContext.with(span);
      OtelHttpAttributes.onStart(span, request, resendCount, config);
      propagator.inject(spanContext, request, REQUEST_HEADER_SETTER);
      OtelHttpAttributes.captureRequestHeaders(span, request, config);
      return new OtelAttempt(span);
    }
  }

  private final class OtelAttempt implements Attempt {

    private final Span span;

    private OtelAttempt(Span span) {
      this.span = span;
    }

    @Override
    public void onResponse(HttpResponse<?> response) {
      OtelHttpAttributes.onResponse(span, response, config);
      span.end();
    }

    @Override
    public void onError(Throwable error) {
      OtelHttpAttributes.onError(span, error);
      span.end();
    }
  }
}
