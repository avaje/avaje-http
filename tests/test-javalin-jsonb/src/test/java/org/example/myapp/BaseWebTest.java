package org.example.myapp;

import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.JacksonBodyAdapter;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class BaseWebTest {

  static Javalin webServer;

  public static String baseUrl;

  @BeforeAll
  public static void init() {
    webServer = Main.start(8887);
    baseUrl = "http://localhost:8887";
  }

  @AfterAll
  public static void shutdown() {
    webServer.stop();
  }

  public static HttpClientContext client() {
    return HttpClientContext.builder()
      .baseUrl(baseUrl)
      .bodyAdapter(new JacksonBodyAdapter())
      .build();
  }
}
