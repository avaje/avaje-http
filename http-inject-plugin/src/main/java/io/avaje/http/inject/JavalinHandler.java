package io.avaje.http.inject;

import static java.util.stream.Collectors.joining;

import java.util.List;

import io.avaje.http.api.AvajeJavalinPlugin;
import io.avaje.http.api.ValidationException;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;

public class JavalinHandler extends AvajeJavalinPlugin {
  @Override
  public void onStart(JavalinConfig config) {
    config.router.mount(r -> r.exception(ValidationException.class, this::handler));
  }

  private void handler(ValidationException ex, Context ctx) {

    var json = ctx.status(ex.getStatus()).jsonMapper();

    List<ValidationException.Violation> violations = ex.getErrors();
    if (json == null) {
      int violationCount = violations.size();
      String violationList =
          violations.stream()
              .map(violation -> "'" + violation.getField() + "' " + violation.getMessage())
              .collect(joining("\n"));

      // return a plain-text error message
      ctx.result(
          String.format(
              "Bad Request [%s validation violations: \n%s]", violationCount, violationList));
      return;
    }
    ctx.json(new ValidationResponse(violations));
  }
}
