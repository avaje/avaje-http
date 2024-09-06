package io.avaje.htmx.nima.jstache;

import io.avaje.htmx.nima.TemplateRender;
import io.jstach.jstachio.JStachio;

public final class JStacheTemplateRender implements TemplateRender {

  @Override
  public String render(Object viewModel) {
    return JStachio.render(viewModel);
  }
}
