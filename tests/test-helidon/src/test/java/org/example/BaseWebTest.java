package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.http.client.RequestLogger;
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

  public static HttpClientContext client() {
    return HttpClientContext.newBuilder()
      .withBaseUrl(baseUrl)
      .withRequestListener(new RequestLogger())
      .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
      //.with(httpClient)
      .build();
  }
}
