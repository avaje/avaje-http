package org.example.web.myapp;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Produces;
import io.avaje.jex.http.Context;
import jakarta.inject.Inject;
import org.example.web.myapp.service.MyService;

/**
 * Request-scoped controller using constructor params for some dependencies
 * and field injection for others.
 */
@Controller("/req-scoped-both")
class ConstructorAndFieldController {

  final MyService service;
  final Context context;

  @Inject AnotherService another;

  ConstructorAndFieldController(MyService service, Context context) {
    this.service = service;
    this.context = context;
  }

  @Produces("text/plain")
  @Get
  String getSimple() {
    return "service=" + service.findAll().size() + " another=" + another.hello() + " url=" + context.fullUrl();
  }
}
