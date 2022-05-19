package org.example.server;

import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;

import java.util.List;

public class Main {

  public static void main(String[] args) {
    start(8090);
  }

  public static Jex.Server start(int port) {
    BeanScope beanScope = BeanScope.builder().build();
    return start(port, beanScope);
  }

  public static Jex.Server start(int port, BeanScope context) {

    final List<Routing.Service> services = context.list(Routing.Service.class);

    final Jex jex = Jex.create();
    jex.routing().addAll(services);
    return jex.port(port).start();
  }
}
