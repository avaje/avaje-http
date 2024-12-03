package org.example;

import io.avaje.http.api.Controller;
import io.avaje.http.api.ExceptionHandler;
import io.avaje.http.api.Filter;
import io.helidon.webserver.http.FilterChain;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.http.RoutingResponse;

import java.util.Map;

/**
 * Controller with only ExceptionHandler methods.
 */
@Controller
final class ErrorController {

  @ExceptionHandler(statusCode = 407)
  Map<String, Object> runEx(RuntimeException ex, ServerRequest req, ServerResponse res) {
    return Map.of("err", String.valueOf(ex));
  }

  @Filter
  void filter1(FilterChain chain, RoutingResponse res) {
    chain.proceed();
  }

  @Filter
  void filter2(FilterChain chain, RoutingResponse res) {
    chain.proceed();
  }

  @Filter
  void filter(FilterChain chain, RoutingResponse res) {
    chain.proceed();
  }
}
