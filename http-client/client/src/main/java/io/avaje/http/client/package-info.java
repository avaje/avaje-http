/**
 * Provides a HTTP client with support for adapting body content
 * (like JSON) to java types.
 * <p>
 * Uses the Java http client
 *
 * <pre>{@code
 *
 *   HttpClientContext ctx = HttpClientContext.newBuilder()
 *       .withBaseUrl("http://localhost:8080")
 *       .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
 *       .build();
 *
 *  HelloDto dto = ctx.request()
 *       .path("hello")
 *       .queryParam("say", "Ki ora")
 *       .get()
 *       .bean(HelloDto.class);
 *
 * }</pre>
 */
package io.avaje.http.client;
