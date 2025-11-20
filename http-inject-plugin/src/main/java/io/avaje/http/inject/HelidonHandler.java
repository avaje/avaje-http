package io.avaje.http.inject;

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

  private void handle(ServerRequest req, ServerResponse res, ValidationException ex) {

    res.status(ex.getStatus()).header("Content-Type", "application/problem+json")
        .send(new ValidationResponse(ex.getStatus(), ex.getErrors(), req.path().rawPath()).toJson());
  }
}
