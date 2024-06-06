package io.avaje.htmx.nima;


import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Template render API for Helidon.
 */
public interface TemplateRender {

  /**
   * Render the given template view model to the server response.
   */
  void render(Object viewModel, ServerRequest req, ServerResponse res);
}
