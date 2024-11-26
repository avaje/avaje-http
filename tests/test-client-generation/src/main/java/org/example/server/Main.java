package org.example.server;

import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;

public class Main {

  public static void main(String[] args) {
    start(8090);
  }

  public static Jex.Server start(int port) {
    BeanScope beanScope = BeanScope.builder().build();
    return start(port, beanScope);
  }

  public static Jex.Server start(int port, BeanScope context) {

    final Jex jex = Jex.create().configureWith(context);
    return jex.port(port).start();
  }
}
