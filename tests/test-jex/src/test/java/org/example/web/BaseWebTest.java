package org.example.web;

import io.avaje.http.client.HttpClient;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.jex.Jex;
import org.example.Main;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.time.Duration;

public class BaseWebTest {

  static Jex.Server webServer;

  public static String baseUrl;

  @BeforeAll
  public static void init() {
    webServer = Main.start(8887);
    baseUrl = "http://localhost:8887";
  }

  @AfterAll
  public static void shutdown() {
    webServer.shutdown();
  }

  public static HttpClient client() {
    return HttpClient.builder()
      .baseUrl(baseUrl)
      .requestTimeout(Duration.ofMinutes(2))
      .bodyAdapter(new JacksonBodyAdapter())
      .build();
  }
}
