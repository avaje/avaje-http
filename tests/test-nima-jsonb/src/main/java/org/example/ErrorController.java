package org.example;

import io.avaje.http.api.Controller;
import io.avaje.http.api.ExceptionHandler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

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

}
