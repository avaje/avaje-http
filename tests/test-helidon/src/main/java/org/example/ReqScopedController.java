package org.example;

import io.dinject.controller.Controller;
import io.dinject.controller.Get;
import io.dinject.controller.Path;
import io.dinject.controller.Produces;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import javax.inject.Inject;

@Controller
@Path("/req-scoped")
public class ReqScopedController {

  @Inject
  ServerRequest request;

  @Inject
  ServerResponse response;

  @Produces("text/plain")
  @Get
  String getSimple() {
    return request.uri().toString() + "-" + response.status();
  }
}
