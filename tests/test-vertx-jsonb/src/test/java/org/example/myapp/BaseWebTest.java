package org.example.myapp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class BaseWebTest {

  static Vertx webServer;
  static HttpClient client;
  static String baseUrl;

  @BeforeAll
  public static void init() {
    webServer = Main.start(8886);
    client = HttpClient.newHttpClient();
    baseUrl = "http://localhost:8886";
  }

  @AfterAll
  public static void shutdown() {
    webServer.close().toCompletionStage().toCompletableFuture().join();
  }

  static HttpResponse<String> get(String path) throws IOException, InterruptedException {
    final HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }
}
