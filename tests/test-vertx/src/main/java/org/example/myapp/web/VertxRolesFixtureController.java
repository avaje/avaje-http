package org.example.myapp.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.ExceptionHandler;
import io.avaje.http.api.Filter;
import io.avaje.http.api.Get;
import io.avaje.http.api.Produces;
import io.vertx.ext.web.RoutingContext;

@Controller("/roles-test")
@Roles(TestRole.ADMIN)
public class VertxRolesFixtureController {

  @Get("/inherited")
  String inherited() {
    return "ok";
  }

  @Get("/explicit")
  @Roles({TestRole.ADMIN, TestRole.AUDITOR})
  String explicit() {
    return "ok";
  }

  @Filter
  void filter(RoutingContext ctx) {
    // noop
  }

  @ExceptionHandler
  String onIllegalArg(IllegalArgumentException ex) {
    return "bad arg";
  }

  @Produces(statusCode = 503)
  @ExceptionHandler(IllegalStateException.class)
  void onIllegalState(RoutingContext ctx) {
    ctx.response().end("bad state");
  }
}
