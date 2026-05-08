package org.example.web.myapp;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Produces;
import io.avaje.jex.http.Context;
import jakarta.inject.Inject;
import org.example.web.myapp.service.MyService;

/**
 * Request-scoped controller with DI field + method injection of request-scoped type.
 * This is a known gap — method injection of request-scoped types is not yet supported
 * by the RequestFactoryWriter.
 */
@Controller("/req-scoped-method")
class MethodInjectReqScopedController {

  @Inject MyService service;

  Context context;

  @Inject
  void setContext(Context context) {
    this.context = context;
  }

  @Produces("text/plain")
  @Get
  String getSimple() {
    return "service=" + service.findAll().size() + " url=" + context.fullUrl();
  }
}
