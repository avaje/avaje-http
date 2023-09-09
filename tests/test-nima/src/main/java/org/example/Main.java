package org.example;

import java.util.List;

import io.avaje.inject.BeanScope;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpFeature;
import io.helidon.webserver.http.HttpRouting;

public class Main {

  public static void main(String[] args) {

    List<HttpFeature> routes = BeanScope.builder().build().list(HttpFeature.class);
    final var builder = HttpRouting.builder();

    routes.forEach(builder::addFeature);

    WebServer.builder().addRouting(builder.build()).build().start();

    System.out.println("started");
  }
}
