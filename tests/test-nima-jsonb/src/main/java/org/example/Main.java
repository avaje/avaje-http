package org.example;

import java.util.List;

import io.avaje.inject.BeanScope;
import io.avaje.jsonb.Jsonb;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpFeature;
import io.helidon.webserver.http.HttpRouting;

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
