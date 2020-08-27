package io.avaje.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.example.webserver.App;
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
      .withBaseUrl(baseUrl)
      .withRequestListener(new RequestLogger())
      .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
      //.with(httpClient)
      .build();
  }
}
