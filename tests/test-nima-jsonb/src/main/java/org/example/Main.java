package org.example;

import java.util.List;

import io.avaje.inject.BeanScope;
import io.avaje.jsonb.Jsonb;
import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.http.HttpFeature;
import io.helidon.nima.webserver.http.HttpRouting;
import io.helidon.nima.webserver.http.HttpService;

public class Main {

  public static void main(String[] args) {

    final var scope = BeanScope.builder().beans(Jsonb.builder().build()).build();
    final List<HttpFeature> list = scope.list(HttpFeature.class);
    final var builder = HttpRouting.builder();
    list.forEach(builder::addFeature);
    final var httpRouting = builder.build();

    WebServer.builder()
        .addRouting(httpRouting)
        // .routing(Main::routing)
        .port(8081)
        .build()
        .start();

    System.out.println("started");
  }
}
