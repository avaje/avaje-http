package org.example.myapp;

import io.avaje.inject.SystemContext;
import io.avaje.http.api.InvalidPathArgumentException;
import io.avaje.http.api.InvalidTypeArgumentException;
import io.avaje.http.api.ValidationException;
import io.avaje.http.api.WebRoutes;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//@ContextModule(name = "app", dependsOn= "validator")
@OpenAPIDefinition(info = @Info(title = "Example service", description = "Example Javalin controllers with Java and Maven"))
public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    start(8082);
  }

  public static Javalin start(int port) {

    Javalin app = Javalin.create(config -> {
      config.showJavalinBanner = false;
      config.logIfServerNotStarted = false;
      config.addStaticFiles("public", Location.CLASSPATH);
      config.accessManager((handler, ctx, permittedRoles) -> {
        log.debug("allow access ...");
        handler.handle(ctx);
      });
    });

    app.exception(ValidationException.class, (exception, ctx) -> {

      Map<String,Object> map = new LinkedHashMap<>();
      map.put("message", exception.getMessage());
      map.put("errors", exception.getErrors());
      ctx.json(map);
      ctx.status(exception.getStatus());
    });

    app.exception(InvalidTypeArgumentException.class, (exception, ctx) -> {

      Map<String, String> map = new LinkedHashMap<>();
      map.put("path", ctx.path());
      map.put("message", "invalid type argument");
      ctx.json(map);
      ctx.status(400);
    });

    app.exception(InvalidPathArgumentException.class, (exception, ctx) -> {

      Map<String, String> map = new LinkedHashMap<>();
      map.put("path", ctx.path());
      map.put("message", "invalid path argument");
      ctx.json(map);
      ctx.status(404);
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
