package io.avaje.http.generator.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Known response types for Http Client methods.
 */
final class KnownResponse {

  private final Map<String, String> map = new HashMap<>();

  KnownResponse() {
    map.put("void", ".asVoid();");
    map.put("java.lang.String", ".asPlainString().body();");

    map.put("java.net.http.HttpResponse<java.lang.Void>", ".asVoid();");
    map.put("java.net.http.HttpResponse<java.lang.String>", ".asString();");
    map.put("java.net.http.HttpResponse<byte[]>", ".asByteArray();");
    map.put("java.net.http.HttpResponse<java.io.InputStream>", ".asInputStream();");
    map.put("java.net.http.HttpResponse<java.util.stream.Stream<java.lang.String>>", ".asLines();");

    map.put("java.util.concurrent.CompletableFuture<java.net.http.HttpResponse<java.lang.Void>>", ".async().asVoid();");
    map.put("java.util.concurrent.CompletableFuture<java.net.http.HttpResponse<java.lang.String>>", ".async().asString();");
    map.put("java.util.concurrent.CompletableFuture<java.net.http.HttpResponse<byte[]>>", ".async().asByteArray();");
    map.put("java.util.concurrent.CompletableFuture<java.net.http.HttpResponse<java.io.InputStream>>", ".async().asInputStream();");
    map.put("java.util.concurrent.CompletableFuture<java.net.http.HttpResponse<java.util.stream.Stream<java.lang.String>>>", ".async().asLines();");

    map.put("io.avaje.http.client.HttpCall<java.net.http.HttpResponse<java.lang.Void>>", ".call().asVoid();");
    map.put("io.avaje.http.client.HttpCall<java.net.http.HttpResponse<java.lang.String>>", ".call().asString();");
    map.put("io.avaje.http.client.HttpCall<java.net.http.HttpResponse<byte[]>>", ".call().asByteArray();");
    map.put("io.avaje.http.client.HttpCall<java.net.http.HttpResponse<java.io.InputStream>>", ".call().asInputStream();");
    map.put("io.avaje.http.client.HttpCall<java.net.http.HttpResponse<java.util.stream.Stream<java.lang.String>>>", ".call().asLines();");

    // Not supported response types - need HttpResponse for these ones
    map.put("byte[]", ".notSupported(); // Use HttpResponse<byte[]> instead?");
    map.put("java.io.InputStream", ".notSupported(); // Use HttpResponse<InputStream> instead?");
    map.put("java.util.stream.Stream<java.lang.String>", ".notSupported(); // Use HttpResponse<Stream<String>> instead?");
    map.put("java.util.concurrent.CompletableFuture<java.lang.Void>", ".notSupported(); // Use CompletableFuture<HttpResponse<Void> instead");
    map.put("java.util.concurrent.CompletableFuture<java.lang.String>", ".notSupported(); // Use CompletableFuture<HttpResponse<String> instead");
    map.put("java.util.concurrent.CompletableFuture<byte[]>", ".notSupported(); // Use CompletableFuture<HttpResponse<byte[]> instead");
    map.put("java.util.concurrent.CompletableFuture<java.io.InputStream>", ".notSupported(); // Use CompletableFuture<HttpResponse<InputStream> instead");
    map.put("java.util.concurrent.CompletableFuture<java.util.stream.Stream<java.lang.String>>", ".notSupported(); // Use CompletableFuture<HttpResponse<Stream<String>> instead");
    map.put("io.avaje.http.client.HttpCall<java.lang.Void>", ".notSupported(); // Use HttpCall<HttpResponse<Void> instead");
    map.put("io.avaje.http.client.HttpCall<java.lang.String>", ".notSupported(); // Use HttpCall<HttpResponse<String> instead");
    map.put("io.avaje.http.client.HttpCall<byte[]>", ".notSupported(); // Use HttpCall<HttpResponse<byte[]> instead");
    map.put("io.avaje.http.client.HttpCall<java.io.InputStream>", ".notSupported(); // Use HttpCall<HttpResponse<InputStream> instead");
    map.put("io.avaje.http.client.HttpCall<java.util.stream.Stream<java.lang.String>>", ".notSupported(); // Use HttpCall<HttpResponse<Stream<String>> instead");
  }

  String get(String full) {
    return map.get(full);
  }
}
