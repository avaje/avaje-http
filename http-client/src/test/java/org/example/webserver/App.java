package org.example.webserver;

import io.avaje.http.api.InvalidPathArgumentException;
import io.avaje.http.api.InvalidTypeArgumentException;
import io.avaje.http.api.ValidationException;
import io.avaje.http.hibernate.validator.BeanValidator;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class App {

  private static final Logger log = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    start(8082);
  }

  public static Javalin start(int port) {
    HelloController$Route bean =
        new HelloController$Route(new HelloController(), new BeanValidator());

    Javalin app =
        Javalin.create(
            config -> {
              config.registerPlugin(bean);
              var route = config.routes;

              route.exception(
                  ValidationException.class,
                  (exception, ctx) -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("message", exception.getMessage());
                    map.put("errors", exception.getErrors());
                    ctx.status(exception.getStatus());
                    ctx.json(map);
                  });

              route.exception(
                  InvalidTypeArgumentException.class,
                  (exception, ctx) -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    map.put("path", ctx.path());
                    map.put("message", "invalid type argument");
                    ctx.status(400);
                    ctx.json(map);
                  });

              route.exception(
                  InvalidPathArgumentException.class,
                  (exception, ctx) -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    map.put("path", ctx.path());
                    map.put("message", "invalid path argument");
                    ctx.status(404);
                    ctx.json(map);
                  });

              route.get("/", ctx -> ctx.result("Hello World"));
              route.head("/head", ctx -> ctx.result("head"));
              route.get("/get", ctx -> ctx.result("get"));
              route.post("/post", ctx -> ctx.result("post"));
              route.put("/put", ctx -> ctx.result("put"));
              route.patch("/patch", ctx -> ctx.result("patch"));
              // route.tra("/patch", ctx -> ctx.result("patch"));
              route.delete("/delete", ctx -> ctx.result("delete body[" + ctx.body().trim() + "]"));
            });

    //    // All WebRoutes / Controllers ... from DI Context
    //    List<WebRoutes> webRoutes = context.getBeans(WebRoutes.class);
    //    app.routes(() -> webRoutes.forEach(WebRoutes::registerRoutes));

    // programmatically create http endpoints

    app.start(port);
    return app;
  }
}
