package org.example;

import io.avaje.http.api.ValidationException;
import io.avaje.http.api.Validator;
import io.avaje.inject.BeanScope;
import io.avaje.inject.InjectModule;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;

import java.util.LinkedHashMap;
import java.util.Map;

@InjectModule(requires = Validator.class)
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
    jex.routing().addAll(context.list(Routing.Service.class));

    jex.exception(ValidationException.class, (exception, ctx) -> {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("message", exception.getMessage());
      map.put("errors", exception.getErrors());
      ctx.status(exception.getStatus());
      ctx.json(map);
    });

    return jex.port(port).start();
  }
}
