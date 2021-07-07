/**
 * Provides a HTTP client with support for adapting body content
 * (like JSON) to java types.
 * <p>
 * Uses the Java http client
 *
 * <pre>{@code
 *
 *   HttpClientContext ctx = HttpClientContext.newBuilder()
 *       .baseUrl("http://localhost:8080")
 *       .bodyAdapter(new JacksonBodyAdapter())
 *       .build();
 *
 *  HelloDto dto = ctx.request()
 *       .path("hello")
 *       .queryParam("say", "Ki ora")
 *       .GET()
 *       .bean(HelloDto.class);
 *
 * }</pre>
 */
package io.avaje.http.client;
