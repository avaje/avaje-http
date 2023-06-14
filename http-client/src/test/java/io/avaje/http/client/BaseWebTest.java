package io.avaje.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.example.webserver.App;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.time.Duration;

public class BaseWebTest {

  static Javalin webServer;

  public static String baseUrl;

  @BeforeAll
  public static void init() {
    webServer = App.start(8889);
    baseUrl = "http://localhost:8889";
  }

  @AfterAll
  public static void shutdown() {
    webServer.stop();
  }

  public static HttpClient client() {
    return HttpClient.builder()
      .baseUrl(baseUrl)
      .connectionTimeout(Duration.ofSeconds(1))
      .requestTimeout(Duration.ofSeconds(1))
      .bodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
      .build();
  }
}
