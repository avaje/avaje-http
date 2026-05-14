package io.avaje.http.client.otel;

import io.avaje.http.client.HttpClientRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

final class OtelConfig {

  private final List<String> capturedRequestHeaders;
  private final List<String> capturedResponseHeaders;
  private final Set<String> knownMethods;
  private final Set<String> sensitiveQueryParameters;
  private final Function<HttpClientRequest, String> urlTemplateResolver;
  private final boolean useLabelAsUrlTemplate;

  OtelConfig(
      List<String> capturedRequestHeaders,
      List<String> capturedResponseHeaders,
      Set<String> knownMethods,
      Set<String> sensitiveQueryParameters,
      Function<HttpClientRequest, String> urlTemplateResolver,
      boolean useLabelAsUrlTemplate) {
    this.capturedRequestHeaders = List.copyOf(capturedRequestHeaders);
    this.capturedResponseHeaders = List.copyOf(capturedResponseHeaders);
    this.knownMethods = Set.copyOf(knownMethods);
    this.sensitiveQueryParameters = Set.copyOf(sensitiveQueryParameters);
    this.urlTemplateResolver = urlTemplateResolver;
    this.useLabelAsUrlTemplate = useLabelAsUrlTemplate;
  }

  List<String> capturedRequestHeaders() {
    return capturedRequestHeaders;
  }

  List<String> capturedResponseHeaders() {
    return capturedResponseHeaders;
  }

  Set<String> knownMethods() {
    return knownMethods;
  }

  Set<String> sensitiveQueryParameters() {
    return sensitiveQueryParameters;
  }

  Function<HttpClientRequest, String> urlTemplateResolver() {
    return urlTemplateResolver;
  }

  boolean useLabelAsUrlTemplate() {
    return useLabelAsUrlTemplate;
  }

  static List<String> normalizeHeaders(Collection<String> headers) {
    Objects.requireNonNull(headers, "headers");
    final List<String> normalized = new ArrayList<>(headers.size());
    for (final String header : headers) {
      normalized.add(header.toLowerCase(Locale.ROOT));
    }
    return List.copyOf(normalized);
  }

  static Set<String> normalizeMethods(Collection<String> methods) {
    Objects.requireNonNull(methods, "methods");
    return Set.copyOf(methods);
  }

  static Set<String> copySensitiveQueryParameters(Collection<String> queryParameters) {
    Objects.requireNonNull(queryParameters, "queryParameters");
    return Set.copyOf(new LinkedHashSet<>(queryParameters));
  }
}
