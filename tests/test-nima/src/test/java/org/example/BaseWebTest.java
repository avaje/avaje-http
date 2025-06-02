package org.example;

import io.avaje.http.client.HttpClient;
import io.avaje.inject.BeanScope;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpFeature;
import io.helidon.webserver.http.HttpRouting;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.time.Duration;
import java.util.List;

public class BaseWebTest {

  static WebServer webServer;

  public static String baseUrl;


  @BeforeAll
  public static void init() {
    List<HttpFeature> routes = BeanScope.builder().build().list(HttpFeature.class);
    final var builder = HttpRouting.builder();
    routes.forEach(builder::addFeature);

    webServer = WebServer.builder().addRouting(builder)
      .port(9067)
      .build()
      .start();

    baseUrl = "http://localhost:9067";
  }

  @AfterAll
  public static void shutdown() {
    webServer.stop();
  }

  public static HttpClient client() {
    return HttpClient.builder()
      .baseUrl(baseUrl)
      .requestTimeout(Duration.ofMinutes(2))
      //.bodyAdapter(new JacksonBodyAdapter())
      .build();
  }
}
