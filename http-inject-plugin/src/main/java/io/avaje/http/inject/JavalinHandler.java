package io.avaje.http.inject;

import java.io.IOException;
import java.io.UncheckedIOException;

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
    try (var os =
        ctx.contentType("application/problem+json").status(ex.getStatus()).outputStream()) {
      new ValidationResponse(ex.getStatus(), ex.getErrors(), ctx.path()).toJson(os);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
