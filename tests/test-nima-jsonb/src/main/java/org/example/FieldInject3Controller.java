package org.example;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.inject.Inject;

@Controller
@Path("field")
final class FieldInject3Controller {

  @Inject ServerResponse response;

  @Get
  String hello() {
    return "hi";
  }
}
