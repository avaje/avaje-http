package org.example.web.myapp;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Produces;
import io.avaje.jex.http.Context;
import jakarta.inject.Inject;

@Controller("/req-scoped")
class ReqScopedController {

  @Inject
  Context context;

  @Produces("text/plain")
  @Get
  String getSimple() {
    return context.fullUrl();
  }
}