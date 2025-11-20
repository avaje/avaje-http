package io.avaje.http.inject;

import static java.util.stream.Collectors.joining;

import io.avaje.http.api.ValidationException;
import io.helidon.webserver.http.HttpFeature;
import io.helidon.webserver.http.HttpRouting.Builder;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class HelidonHandler implements HttpFeature {

  @Override
  public void setup(Builder routing) {

    routing.error(ValidationException.class, this::handle);
  }

  private void handle(ServerRequest req, ServerResponse res, ValidationException exception) {
    var violations = exception.getErrors();

    int violationCount = violations.size();
    String violationList =
        violations.stream()
            .map(violation -> "'" + violation.getField() + "' " + violation.getMessage())
            .collect(joining("\n"));

    res.status(exception.getStatus())
        .send(
            String.format(
                    "Bad Request [%s validation violations: \n%s]", violationCount, violationList)
                .getBytes());
  }
}
