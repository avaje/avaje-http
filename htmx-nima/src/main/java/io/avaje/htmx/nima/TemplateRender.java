package io.avaje.htmx.nima;


import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public interface TemplateRender {

  void render(Object viewModel, ServerRequest req, ServerResponse res);
}
