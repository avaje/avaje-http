package io.avaje.htmx.nima.jstache;

import io.avaje.htmx.nima.TemplateRender;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.jstach.jstachio.JStachio;

public final class JStacheTemplateRender implements TemplateRender {

  @Override
  public void render(Object viewModel, ServerRequest req, ServerResponse res) {
    var content = JStachio.render(viewModel);
    res.send(content);
  }
}
