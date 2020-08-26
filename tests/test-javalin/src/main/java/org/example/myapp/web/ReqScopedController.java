package org.example.myapp.web;

import io.dinject.controller.Controller;
import io.dinject.controller.Get;
import io.dinject.controller.Path;
import io.dinject.controller.Produces;
import io.javalin.http.Context;

import javax.inject.Inject;

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
