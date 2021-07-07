package io.avaje.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.webserver.App;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class BaseWebTest {

  static Javalin webServer;

  public static String baseUrl;

  @BeforeAll
  public static void init() {
    webServer = App.start(8887);
    baseUrl = "http://localhost:8887";
  }

  @AfterAll
  public static void shutdown() {
    webServer.stop();
  }

  public static HttpClientContext client() {
    return HttpClientContext.newBuilder()
      .baseUrl(baseUrl)
      .bodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
      .build();
  }
}
