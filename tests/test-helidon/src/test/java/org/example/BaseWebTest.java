package org.example;

import io.avaje.http.client.HttpClient;
import io.avaje.http.client.JacksonBodyAdapter;
import io.helidon.webserver.WebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class BaseWebTest {

  static WebServer webServer;

  public static String baseUrl;

  @BeforeAll
  public static void init() {
    webServer = Main.startServer(8889);
    baseUrl = "http://localhost:8889";
  }

  @AfterAll
  public static void shutdown() {
    webServer.shutdown();
  }

  public static HttpClient client() {
    return HttpClient.builder()
      .baseUrl(baseUrl)
      .bodyAdapter(new JacksonBodyAdapter())
      .build();
  }
}
