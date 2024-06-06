package org.example.htmx.template;

import io.avaje.htmx.nima.TemplateRender;
import io.avaje.inject.Component;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.jstach.jstachio.JStachio;

@Component
public class JstacheTemplateRender implements TemplateRender {

  @Override
  public void render(Object viewModel, ServerRequest req, ServerResponse res) {
    String content = JStachio.render(viewModel);
    res.send(content);
  }
}
