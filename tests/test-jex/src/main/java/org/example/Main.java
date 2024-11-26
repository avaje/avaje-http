package org.example;

import io.avaje.http.api.ValidationException;
import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;

import java.util.LinkedHashMap;
import java.util.Map;

public class Main {

  public static void main(String[] args) {
    start(8090);
  }

  public static Jex.Server start(int port) {
    BeanScope beanScope = BeanScope.builder().build();
    return start(port, beanScope);
  }

  public static Jex.Server start(int port, BeanScope context) {
    final Jex jex = Jex.create();
    jex.routing().addAll(context.list(Routing.HttpService.class));

    jex.routing().error(ValidationException.class, (exception, ctx) -> {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("message", exception.getMessage());
      map.put("errors", exception.getErrors());
      ctx.status(exception.getStatus());
      ctx.json(map);
    });

    return jex.port(port).start();
  }
}
