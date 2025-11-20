package io.avaje.http.inject;

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

    ctx.contentType("application/problem+json")
        .write(new ValidationResponse(ex.getStatus(), ex.getErrors(), ctx.path()).toJson());
  }
}
