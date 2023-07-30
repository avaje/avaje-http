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
              config.showJavalinBanner = false;
              config.accessManager(
                  (handler, ctx, permittedRoles) -> {
                    log.debug("allow access ...");
                    handler.handle(ctx);
                  });
              config.plugins.register(bean);
            });

    app.exception(
        ValidationException.class,
        (exception, ctx) -> {
          Map<String, Object> map = new LinkedHashMap<>();
          map.put("message", exception.getMessage());
          map.put("errors", exception.getErrors());
          ctx.status(exception.getStatus());
          ctx.json(map);
        });

    app.exception(
        InvalidTypeArgumentException.class,
        (exception, ctx) -> {
          Map<String, String> map = new LinkedHashMap<>();
          map.put("path", ctx.path());
          map.put("message", "invalid type argument");
          ctx.status(400);
          ctx.json(map);
        });

    app.exception(
        InvalidPathArgumentException.class,
        (exception, ctx) -> {
          Map<String, String> map = new LinkedHashMap<>();
          map.put("path", ctx.path());
          map.put("message", "invalid path argument");
          ctx.status(404);
          ctx.json(map);
        });

    app.get("/", ctx -> ctx.result("Hello World"));
    app.head("/head", ctx -> ctx.result("head"));
    app.get("/get", ctx -> ctx.result("get"));
    app.post("/post", ctx -> ctx.result("post"));
    app.put("/put", ctx -> ctx.result("put"));
    app.patch("/patch", ctx -> ctx.result("patch"));
    // app.tra("/patch", ctx -> ctx.result("patch"));
    app.delete("/delete", ctx -> ctx.result("delete body[" + ctx.body().trim() + "]"));

    //    // All WebRoutes / Controllers ... from DI Context
    //    List<WebRoutes> webRoutes = context.getBeans(WebRoutes.class);
    //    app.routes(() -> webRoutes.forEach(WebRoutes::registerRoutes));

    // programmatically create http endpoints

    app.start(port);
    return app;
  }
}
