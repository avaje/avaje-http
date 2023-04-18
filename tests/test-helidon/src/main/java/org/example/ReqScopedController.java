package org.example;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import jakarta.inject.Inject;

@Controller(instrumentRequestContext = true)
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
