package org.example.web;

import io.avaje.inject.Component;
import io.avaje.jex.htmx.TemplateRender;
import io.jstach.jstache.JStache;
import io.jstach.jstachio.JStachio;

@Component
public class MyTemplateRender implements TemplateRender {
  @Override
  public String render(Object viewModel) {
    return JStachio.render(viewModel);
  }
}
