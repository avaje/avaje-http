package org.example;

import java.util.List;

import io.avaje.inject.BeanScope;
import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.http.HttpRouting;
import io.helidon.nima.webserver.http.HttpService;

public class Main {

  public static void main(String[] args) {

    final var scope = BeanScope.builder().build();
    final List<HttpService> list = scope.list(HttpService.class);
    final var builder = HttpRouting.builder();
    for (final HttpService httpService : list) {
      httpService.routing(builder);
    }
    final var httpRouting = builder.build();


    WebServer.builder()
      .addRouting(httpRouting)
      //.routing(Main::routing)
      .port(8081)
      .build()
      .start();

    System.out.println("started");
  }
}
