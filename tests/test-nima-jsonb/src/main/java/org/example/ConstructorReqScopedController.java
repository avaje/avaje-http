package org.example;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Request-scoped controller with constructor params: generic DI dependency + request-scoped types.
 */
@Controller
@Path("ctor-req")
final class ConstructorReqScopedController {

  final GenericService<String> service;
  final ServerRequest request;
  final ServerResponse response;

  ConstructorReqScopedController(GenericService<String> service, ServerRequest request, ServerResponse response) {
    this.service = service;
    this.request = request;
    this.response = response;
  }

  @Get
  String hello() {
    return "size=" + service.findAll().size();
  }
}
