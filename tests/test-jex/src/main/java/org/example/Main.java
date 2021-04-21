package org.example;

import io.avaje.inject.BeanContext;
import io.avaje.inject.SystemContext;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;

public class Main {

  public static void main(String[] args) {
    start(8090);
  }

  public static Jex.Server start(int port) {
    return start(port, SystemContext.context());
  }

  public static Jex.Server start(int port, BeanContext context) {
    final Jex jex = Jex.create();
    jex.routing().addAll(context.getBeans(Routing.Service.class));
    return jex.port(port).start();
  }
}
