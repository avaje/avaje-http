package io.avaje.http.inject;

import java.io.IOException;
import java.io.UncheckedIOException;

import io.avaje.http.api.ValidationException;
import io.avaje.jex.Routing;
import io.avaje.jex.Routing.HttpService;
import io.avaje.jex.http.Context;

final class JexHandler implements HttpService {

  @Override
  public void add(Routing arg0) {
    arg0.error(ValidationException.class, this::handler);
  }

  private void handler(Context ctx, ValidationException ex) {
    try (var os =
        ctx.contentType("application/problem+json").status(ex.getStatus()).outputStream()) {
      new ValidationResponse(ex.getStatus(), ex.getErrors(), ctx.path()).toJson(os);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
