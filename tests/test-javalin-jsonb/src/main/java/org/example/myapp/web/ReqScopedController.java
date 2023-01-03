package org.example.myapp.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import io.javalin.http.Context;

import jakarta.inject.Inject;

@Controller
@Path("/req-scoped")
class ReqScopedController {

  @Inject
  Context context;

  @Produces("text/plain")
  @Get
  String getSimple() {
    return context.url();
  }
}
