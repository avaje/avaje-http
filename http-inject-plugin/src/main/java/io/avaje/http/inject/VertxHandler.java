package io.avaje.http.inject;

import io.avaje.http.api.ValidationException;
import io.avaje.http.api.vertx.VertxRouteSet;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

final class VertxHandler implements VertxRouteSet {

  @Override
  public void register(io.vertx.ext.web.Router router) {
    router.route().failureHandler(this::handle);
  }

  private void handle(RoutingContext ctx) {
    final var failure = ctx.failure();
    if (!(failure instanceof ValidationException)) {
      ctx.next();
      return;
    }
    final var ex = (ValidationException) failure;
    try (var os = new ByteArrayOutputStream()) {
      new ValidationResponse(ex.getStatus(), ex.getErrors(), ctx.request().path()).toJson(os);
      ctx.response()
        .putHeader("Content-Type", "application/problem+json")
        .setStatusCode(ex.getStatus())
        .end(Buffer.buffer(os.toByteArray()));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
