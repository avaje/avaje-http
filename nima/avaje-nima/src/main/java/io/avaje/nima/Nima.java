package io.avaje.nima;

import io.avaje.inject.BeanScope;
import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.http.HttpRouting;
import io.helidon.nima.webserver.http.HttpService;

public class Nima {

  private WebServer.Builder webServerBuilder;
  private WebServer webServer;

  public Nima configure(BeanScope beanScope) {
    HttpRouting.Builder routeBuilder = beanScope.getOptional(HttpRouting.Builder.class)
      .orElse(HttpRouting.builder());

    for (final HttpService httpService : beanScope.list(HttpService.class)) {
      httpService.routing(routeBuilder);
    }

    webServerBuilder = beanScope.getOptional(WebServer.Builder.class).orElse(WebServer.builder());
    webServerBuilder.addRouting(routeBuilder.build());
    return this;
  }

  public Nima port(int port) {
    webServerBuilder.port(port);
    return this;
  }

  public void start() {
    this.webServer = webServerBuilder.start();
  }

  public void start(int port) {
    this.webServer = webServerBuilder.port(port).start();
  }

  public int port() {
    return webServer.port();
  }

  public void stop() {
    webServer.stop();
  }
}
