package org.example;

import io.avaje.inject.BeanContext;
import io.avaje.inject.SystemContext;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;

import java.util.List;

public class Main {

  public static void main(String[] args) {

    start(8090);
  }

  public static Jex.Server start(int port) {
    final Jex jex = Jex.create();

    final Routing routing = jex.routing();
    final BeanContext context = SystemContext.context();
    final List<Routing.Service> beans = context.getBeans(Routing.Service.class);
    beans.forEach(service -> service.add(routing));

    return jex.port(port).start();
  }
}
