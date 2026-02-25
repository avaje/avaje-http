package io.avaje.http.inject;

import java.io.IOException;
import java.io.UncheckedIOException;

import io.avaje.http.api.AvajeJavalinPlugin;
import io.avaje.http.api.ValidationException;
import io.javalin.config.JavalinState;
import io.javalin.http.Context;

final class JavalinHandler extends AvajeJavalinPlugin {

  @Override
  public void onStart(JavalinState config) {
    config.routes.exception(ValidationException.class, this::handler);
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
