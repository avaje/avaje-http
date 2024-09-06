package org.example.htmx.template;

import io.avaje.htmx.nima.TemplateRender;
import io.avaje.inject.Component;
import io.jstach.jstachio.JStachio;

@Component
public class JstacheTemplateRender implements TemplateRender {

  @Override
  public String render(Object viewModel) {
    return JStachio.render(viewModel);
  }
}
