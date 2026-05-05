package org.example.web.myapp;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Produces;
import io.avaje.jex.http.Context;
import org.example.web.myapp.service.MyService;

/**
 * Request-scoped controller with constructor DI dependency + request-scoped param.
 */
@Controller("/req-scoped-ctor")
class ConstructorReqScopedController {

  final MyService service;
  final Context context;

  ConstructorReqScopedController(MyService service, Context context) {
    this.service = service;
    this.context = context;
  }

  @Produces("text/plain")
  @Get
  String getSimple() {
    return "service=" + service.findAll().size() + " url=" + context.fullUrl();
  }
}
