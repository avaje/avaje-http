package io.avaje.http.inject;

import static java.util.stream.Collectors.joining;

import java.util.List;

import io.avaje.http.api.ValidationException;
import io.avaje.jex.Routing;
import io.avaje.jex.Routing.HttpService;
import io.avaje.jex.http.Context;

public class JexHandler implements HttpService {

  @Override
  public void add(Routing arg0) {

    arg0.error(ValidationException.class, this::handler);
  }

  private void handler(Context ctx, ValidationException ex) {

    var json = ctx.status(ex.getStatus()).jsonService();

    List<ValidationException.Violation> violations = ex.getErrors();
    if (json == null) {
      int violationCount = violations.size();
      String violationList =
          violations.stream()
              .map(violation -> "'" + violation.getField() + "' " + violation.getMessage())
              .collect(joining("\n"));

      // return a plain-text error message
      ctx.text(
          String.format(
              "Bad Request [%s validation violations: \n%s]", violationCount, violationList));
      return;
    }
    ctx.contentType("application/problem+json")
        .write(json.toJsonString(new ValidationResponse(ex.getStatus(), violations, ctx.path())));
  }
}
