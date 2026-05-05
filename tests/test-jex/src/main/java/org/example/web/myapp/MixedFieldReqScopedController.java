package org.example.web.myapp;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Produces;
import io.avaje.jex.http.Context;
import jakarta.inject.Inject;
import org.example.web.myapp.service.MyService;

/**
 * Request-scoped controller with mixed field injection — both DI and request-scoped.
 */
@Controller("/req-scoped-mixed")
class MixedFieldReqScopedController {

  @Inject MyService service;
  @Inject Context context;

  @Produces("text/plain")
  @Get
  String getSimple() {
    return "service=" + service.findAll().size() + " url=" + context.fullUrl();
  }
}
