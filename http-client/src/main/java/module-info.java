/**
 * Provides a HTTP client backed by java's built in {@link java.net.http.HttpClient} with support for adapting body content
 * (like JSON) to java types.
 * <p>
 * Uses the Java http client
 *
 * <pre>{@code
 *
 *   HttpClient client = HttpClient.builder()
 *       .baseUrl("http://localhost:8080")
 *       .bodyAdapter(new JacksonBodyAdapter())
 *       .build();
 *
 *  HelloDto dto = client.request()
 *       .path("hello")
 *       .queryParam("say", "Whats up")
 *       .GET()
 *       .bean(HelloDto.class);
 *
 * }</pre>
 */
module io.avaje.http.client {

  uses io.avaje.http.client.HttpClient.GeneratedComponent;

  requires transitive java.net.http;
  requires transitive io.avaje.applog;
  requires static com.fasterxml.jackson.databind;
  requires static com.fasterxml.jackson.annotation;
  requires static com.fasterxml.jackson.core;
  requires static io.avaje.jsonb;
  requires static io.avaje.inject;
  requires static jdk.httpserver;

  exports io.avaje.http.client;
}
