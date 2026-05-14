package io.avaje.http.client.otel;

import io.avaje.http.client.HttpClient;
import io.avaje.http.client.HttpClientRequest;
import io.avaje.http.client.RequestObserver;
import io.opentelemetry.api.OpenTelemetry;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Configure OpenTelemetry request observation for avaje-http-client.
 */
public final class AvajeHttpClientTelemetry {

  private final RequestObserver requestObserver;

  /**
   * Create using default configuration.
   *
   * @param openTelemetry The OpenTelemetry instance to use
   * @return The configured telemetry helper
   */
  public static AvajeHttpClientTelemetry create(OpenTelemetry openTelemetry) {
    return builder(openTelemetry).build();
  }

  /**
   * Return a builder for OpenTelemetry configuration.
   *
   * @param openTelemetry The OpenTelemetry instance to use
   * @return The telemetry builder
   */
  public static Builder builder(OpenTelemetry openTelemetry) {
    return new Builder(openTelemetry);
  }

  private AvajeHttpClientTelemetry(RequestObserver requestObserver) {
    this.requestObserver = requestObserver;
  }

  /**
   * Return the configured request observer.
   *
   * @return The request observer
   */
  public RequestObserver requestObserver() {
    return requestObserver;
  }

  /**
   * Apply the request observer to the given builder.
   *
   * @param builder The builder to configure
   * @return The configured builder
   */
  public HttpClient.Builder configure(HttpClient.Builder builder) {
    Objects.requireNonNull(builder, "builder");
    return builder.requestObserver(requestObserver);
  }

  /**
   * Builder for OpenTelemetry configuration.
   */
  public static final class Builder {

    private static final Set<String> DEFAULT_KNOWN_METHODS = Set.of(
      "CONNECT", "DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT", "QUERY", "TRACE");

    private static final Set<String> DEFAULT_SENSITIVE_QUERY_PARAMETERS = Set.of(
      "AWSAccessKeyId", "Signature", "sig", "X-Goog-Signature");

    private final OpenTelemetry openTelemetry;
    private List<String> capturedRequestHeaders = List.of();
    private List<String> capturedResponseHeaders = List.of();
    private Set<String> knownMethods = DEFAULT_KNOWN_METHODS;
    private Set<String> sensitiveQueryParameters = DEFAULT_SENSITIVE_QUERY_PARAMETERS;
    private Function<HttpClientRequest, String> urlTemplateResolver;
    private boolean useLabelAsUrlTemplate;

    private Builder(OpenTelemetry openTelemetry) {
      this.openTelemetry = Objects.requireNonNull(openTelemetry, "openTelemetry");
    }

    /**
     * Capture request headers as span attributes.
     *
     * @param requestHeaders The request headers to capture
     * @return This builder
     */
    public Builder capturedRequestHeaders(Collection<String> requestHeaders) {
      this.capturedRequestHeaders = OtelConfig.normalizeHeaders(requestHeaders);
      return this;
    }

    /**
     * Capture response headers as span attributes.
     *
     * @param responseHeaders The response headers to capture
     * @return This builder
     */
    public Builder capturedResponseHeaders(Collection<String> responseHeaders) {
      this.capturedResponseHeaders = OtelConfig.normalizeHeaders(responseHeaders);
      return this;
    }

    /**
     * Override the known HTTP methods used for span naming and method attributes.
     *
     * @param knownMethods The methods to recognise
     * @return This builder
     */
    public Builder knownMethods(Collection<String> knownMethods) {
      this.knownMethods = OtelConfig.normalizeMethods(knownMethods);
      return this;
    }

    /**
     * Override the set of query parameter names whose values should be redacted.
     *
     * @param sensitiveQueryParameters The query parameter names to redact
     * @return This builder
     */
    public Builder sensitiveQueryParameters(Collection<String> sensitiveQueryParameters) {
      this.sensitiveQueryParameters = OtelConfig.copySensitiveQueryParameters(sensitiveQueryParameters);
      return this;
    }

    /**
     * Use a custom low-cardinality URL template resolver.
     *
     * @param urlTemplateResolver The resolver to use
     * @return This builder
     */
    public Builder urlTemplateResolver(Function<HttpClientRequest, String> urlTemplateResolver) {
      this.urlTemplateResolver = Objects.requireNonNull(urlTemplateResolver, "urlTemplateResolver");
      return this;
    }

    /**
     * Use {@link HttpClientRequest#label()} as the URL template when present.
     *
     * @param useLabelAsUrlTemplate Set to true to use request labels as URL templates
     * @return This builder
     */
    public Builder useLabelAsUrlTemplate(boolean useLabelAsUrlTemplate) {
      this.useLabelAsUrlTemplate = useLabelAsUrlTemplate;
      return this;
    }

    /**
     * Build the telemetry helper.
     *
     * @return The configured telemetry helper
     */
    public AvajeHttpClientTelemetry build() {
      final OtelConfig config = new OtelConfig(
        capturedRequestHeaders,
        capturedResponseHeaders,
        knownMethods,
        sensitiveQueryParameters,
        urlTemplateResolver,
        useLabelAsUrlTemplate);
      return new AvajeHttpClientTelemetry(new OtelRequestObserver(openTelemetry, config));
    }
  }
}
