package io.avaje.http.generator.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Known response types for Http Client methods.
 */
class KnownResponse {

  private final Map<String, String> map = new HashMap<>();

  KnownResponse() {
    map.put("void", ".asVoid();");
    map.put("java.net.http.HttpResponse<java.lang.Void>", ".asVoid();");
    map.put("java.net.http.HttpResponse<java.lang.String>", ".asString();");
    map.put("java.lang.String", ".asString().body();");
    map.put("java.net.http.HttpResponse<java.io.InputStream>", ".asInputStream();");
    map.put("java.io.InputStream", ".asInputStream().body();");
    map.put("java.net.http.HttpResponse<java.util.Stream<java.lang.String>>", ".asLines();");
    map.put("java.util.Stream<java.lang.String>", ".asLines().body();");
    map.put("java.net.http.HttpResponse<byte[]>", ".asByteArray();");
    map.put("byte[]", ".asByteArray().body();");
    map.put("java.util.concurrent.CompletableFuture<java.lang.Void>", ".async().asVoid();");
    map.put("java.util.concurrent.CompletableFuture<java.net.http.HttpResponse<java.lang.Void>>", ".async().asVoid();");
    map.put("java.util.concurrent.CompletableFuture<java.net.http.HttpResponse<java.lang.String>>", ".async().asString();");
    map.put("io.avaje.http.client.HttpCall<java.net.http.HttpResponse<java.lang.Void>>", ".call().asVoid();");
    map.put("io.avaje.http.client.HttpCall<java.net.http.HttpResponse<java.lang.String>>", ".call().asString();");
  }

  String get(String full) {
    return map.get(full);
  }
}
