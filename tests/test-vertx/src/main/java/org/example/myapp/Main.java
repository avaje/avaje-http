package org.example.myapp;

import java.util.List;

import io.avaje.http.api.vertx.VertxRouteSet;
import io.avaje.inject.BeanScope;
import io.avaje.inject.InjectModule;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

@InjectModule(name = "app")
public class Main {

  public static void main(String[] args) {
    start(8084);
  }

  public static Vertx start(int port) {
    final BeanScope scope = BeanScope.builder().build();
    final List<VertxRouteSet> routeSets = scope.list(VertxRouteSet.class);

    final Vertx vertx = Vertx.vertx();
    final Router router = Router.router(vertx);

    routeSets.forEach(routeSet -> routeSet.register(router));
    router.get("/").handler(ctx -> ctx.response().end("Hello World"));

    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(port)
      .toCompletionStage()
      .toCompletableFuture()
      .join();

    return vertx;
  }
}
