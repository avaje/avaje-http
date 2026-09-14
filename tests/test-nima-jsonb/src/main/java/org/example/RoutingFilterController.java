package org.example;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Filter;
import io.helidon.webserver.http.FilterChain;
import io.helidon.webserver.http.RoutingRequest;
import io.helidon.webserver.http.RoutingResponse;

@Controller
final class RoutingFilterController {

  @Filter
  void filter(FilterChain chain, RoutingRequest request, RoutingResponse response) {
    chain.proceed();
  }
}
