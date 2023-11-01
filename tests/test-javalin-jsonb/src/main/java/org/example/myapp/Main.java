package org.example.myapp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.avaje.http.api.AvajeJavalinPlugin;
import io.avaje.http.api.InvalidPathArgumentException;
import io.avaje.http.api.InvalidTypeArgumentException;
import io.avaje.http.api.ValidationException;
import io.avaje.http.api.Validator;
import io.avaje.inject.BeanScope;
import io.avaje.inject.InjectModule;
import io.avaje.inject.spi.GenericType;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.Plugin;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@InjectModule(name = "app", requires = Validator.class)
@OpenAPIDefinition(info = @Info(title = "Example service", description = "Example Javalin controllers with Java and Maven"))
@SecurityScheme(type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.QUERY, name = "JWT", paramName = "access_token", description = "JSON Web Tokens are an open, industry standard RFC 7519 method for representing claims securely between two parties.")
public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    start(8082);
  }

  public static Javalin start(int port) {

    // All WebRoutes / Controllers ... from DI Context
    final var beanScope = BeanScope.builder().build();
    final var webRoutes = beanScope.list(AvajeJavalinPlugin.class);

    final var app =
        Javalin.create(
            config -> {
              config.showJavalinBanner = false;
              config.staticFiles.add("public", Location.CLASSPATH);
              //              config.accessManager(
              //                  (handler, ctx, permittedRoles) -> {
              //                    log.debug("allow access ...");
              //                    handler.handle(ctx);
              //                  });
              webRoutes.forEach(config::registerPlugin);
            });

    app.exception(ValidationException.class, (exception, ctx) -> {

      final Map<String, Object> map = new LinkedHashMap<>();
      map.put("message", exception.getMessage());
      map.put("errors", exception.getErrors());
      ctx.json(map);
      ctx.status(exception.getStatus());
    });

    app.exception(InvalidTypeArgumentException.class, (exception, ctx) -> {

      final Map<String, String> map = new LinkedHashMap<>();
      map.put("path", ctx.path());
      map.put("message", "invalid type argument");
      ctx.json(map);
      ctx.status(400);
    });

    app.exception(InvalidPathArgumentException.class, (exception, ctx) -> {

      final Map<String, String> map = new LinkedHashMap<>();
      map.put("path", ctx.path());
      map.put("message", "invalid path argument");
      ctx.json(map);
      ctx.status(404);
    });


    app.get("/", ctx -> {
      ctx.result("Hello World");
    });

    app.start(port);
    return app;
  }
}
