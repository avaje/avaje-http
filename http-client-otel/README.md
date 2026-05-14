# avaje-http-client-otel

Optional OpenTelemetry support for `avaje-http-client`.

This module instruments outbound requests made through `avaje-http-client` using the
`RequestObserver` lifecycle added in core.

It is intended for applications that:

- already use `avaje-http-client`
- want explicit OpenTelemetry integration at the avaje layer
- want minimal OpenTelemetry runtime dependencies
- want one span per actual send attempt, including retries
- want to use avaje-specific low-cardinality metadata such as `HttpClientRequest.label()`

## What it does

`avaje-http-client-otel`:

- creates outbound OpenTelemetry `CLIENT` spans
- injects W3C trace context headers into outgoing requests
- works with sync, async, and retry execution
- creates one span per actual send attempt
- can use `label()` or a custom resolver as `url.template`
- captures explicitly configured request and response headers
- redacts credentials and known sensitive query parameters in `url.full`

At runtime it depends on:

- `io.avaje:avaje-http-client`
- `io.opentelemetry:opentelemetry-api`

Your application still needs a configured `OpenTelemetry` instance, typically backed by the
OpenTelemetry SDK or your existing application OpenTelemetry setup.

## Dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-http-client-otel</artifactId>
  <version>${avaje.http.version}</version>
</dependency>
```

## Basic usage

```java
import io.avaje.http.client.HttpClient;
import io.avaje.http.client.otel.AvajeHttpClientTelemetry;

var client =
  AvajeHttpClientTelemetry.builder(openTelemetry)
    .useLabelAsUrlTemplate(true)
    .capturedRequestHeaders(List.of("x-request-id"))
    .capturedResponseHeaders(List.of("x-request-id"))
    .build()
    .configure(
      HttpClient.builder()
        .baseUrl("https://api.example.com"))
    .build();
```

If you want low-cardinality span names and `url.template`, set a request label:

```java
client.request()
  .label("/customers/{id}")
  .path("customers")
  .path(customerId)
  .GET()
  .asString();
```

Or supply your own resolver:

```java
var telemetry =
  AvajeHttpClientTelemetry.builder(openTelemetry)
    .urlTemplateResolver(request -> "/customers/{id}")
    .build();
```

## What it records

The module records standard outbound HTTP client span data such as:

- `http.request.method`
- `http.request.method_original` for non-standard methods
- `url.full`
- `server.address`
- `server.port`
- `http.response.status_code`
- `error.type`
- `http.request.resend_count` for retries
- configured request and response headers

Span names default to the HTTP method, or `METHOD + template` when a low-cardinality template is
available.

## Choosing between the three approaches

There are three reasonable ways to get OpenTelemetry spans for avaje HTTP client calls.

| Option | Use when | Notes |
|---|---|---|
| `avaje-http-client-otel` | You use `avaje-http-client` directly and want avaje-aware instrumentation with minimal runtime dependencies | Explicit opt-in, one span per actual send attempt, understands avaje labels/templates |
| OpenTelemetry Java agent | You want automatic instrumentation across the whole application with no code changes | Operationally simple if the agent is already in use, but it is broader and less avaje-specific |
| OpenTelemetry JDK `HttpClient` wrapper (`JavaHttpClientTelemetry`) | You already instrument raw JDK `HttpClient` usage or want to wrap and supply a custom JDK client yourself | Uses upstream OpenTelemetry library instrumentation, but does not know about avaje request labels or avaje-specific request lifecycle |

## When this module is preferred

Prefer `avaje-http-client-otel` when:

- `avaje-http-client` is your main HTTP client API
- you want a smaller runtime dependency footprint than the OpenTelemetry instrumentation library
- you want retries represented as separate send-attempt spans
- you want avaje request labels or a custom avaje resolver to drive low-cardinality naming

## When the Java agent is preferred

Prefer the OpenTelemetry Java agent when:

- you already run the agent in production
- you want zero application code changes
- you want broader automatic instrumentation across frameworks and libraries

The Java agent is a good default when your goal is automatic coverage, not avaje-specific control.

## When the JDK `HttpClient` wrapper is preferred

Prefer the upstream OpenTelemetry JDK wrapper when:

- you are already wrapping raw JDK `HttpClient` instances elsewhere
- you want to reuse the upstream OpenTelemetry-maintained wrapper directly
- you are happy instrumenting at the JDK client layer rather than the avaje layer

Typical usage looks like:

```java
var jdkClient =
  JavaHttpClientTelemetry.create(openTelemetry)
    .wrap(java.net.http.HttpClient.newBuilder().build());

var client =
  HttpClient.builder()
    .client(jdkClient)
    .baseUrl("https://api.example.com")
    .build();
```

When using this approach through `HttpClient.Builder.client(...)`, configure the JDK client
settings on the wrapped JDK client itself.

## Do not combine the approaches for the same outbound call path

In general, choose one of:

1. `avaje-http-client-otel`
2. OpenTelemetry Java agent
3. OpenTelemetry JDK `HttpClient` wrapper

If you stack them on the same request path, you are likely to get duplicate spans.

## Notes

- Request and response header capture is explicit allow-list only.
- `label()` should only be used as a span template when it is low cardinality.
- This module focuses on outbound client spans and propagation, not broad application
  auto-instrumentation.
