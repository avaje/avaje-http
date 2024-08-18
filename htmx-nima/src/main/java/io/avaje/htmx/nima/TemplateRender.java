package io.avaje.htmx.nima;

/**
 * Template render API for Helidon.
 */
public interface TemplateRender {

  /**
   * Render the given template view model to the server response.
   */
  String render(Object viewModel);
}
