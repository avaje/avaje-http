package org.example;

import io.avaje.inject.ApplicationScope;
import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;

public class Main {

  public static void main(String[] args) {
    start(8090);
  }

  public static Jex.Server start(int port) {
    return start(port, ApplicationScope.scope());
  }

  public static Jex.Server start(int port, BeanScope context) {
    final Jex jex = Jex.create();
    jex.routing().addAll(context.list(Routing.Service.class));
    return jex.port(port).start();
  }
}
