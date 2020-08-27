package org.example.webserver;

import io.dinject.SystemContext;
import io.dinject.controller.InvalidPathArgumentException;
import io.dinject.controller.InvalidTypeArgumentException;
import io.dinject.controller.ValidationException;
import io.dinject.controller.WebRoutes;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class App {

  private static final Logger log = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    start(8082);
  }

  public static Javalin start(int port) {

    Javalin app = Javalin.create(config -> {
      config.showJavalinBanner = false;
      config.accessManager((handler, ctx, permittedRoles) -> {
        log.debug("allow access ...");
        handler.handle(ctx);
      });
    });

    app.exception(ValidationException.class, (exception, ctx) -> {

      Map<String,Object> map = new LinkedHashMap<>();
      map.put("message", exception.getMessage());
      map.put("errors", exception.getErrors());
      ctx.status(exception.getStatus());
      ctx.json(map);
    });

    app.exception(InvalidTypeArgumentException.class, (exception, ctx) -> {

      Map<String, String> map = new LinkedHashMap<>();
      map.put("path", ctx.path());
      map.put("message", "invalid type argument");
      ctx.status(400);
      ctx.json(map);
    });

    app.exception(InvalidPathArgumentException.class, (exception, ctx) -> {

      Map<String, String> map = new LinkedHashMap<>();
      map.put("path", ctx.path());
      map.put("message", "invalid path argument");
      ctx.status(404);
      ctx.json(map);
    });


    app.get("/", ctx -> {
      ctx.result("Hello World");
    });

    // All WebRoutes / Controllers ... from DI Context
    List<WebRoutes> webRoutes = SystemContext.getBeans(WebRoutes.class);
    app.routes(() -> webRoutes.forEach(WebRoutes::registerRoutes));

    app.start(port);
    return app;
  }
}
