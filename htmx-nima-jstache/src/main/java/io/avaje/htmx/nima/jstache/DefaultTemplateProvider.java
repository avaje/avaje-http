package io.avaje.htmx.nima.jstache;

import io.avaje.htmx.nima.TemplateRender;
import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.Plugin;

/**
 * Plugin for avaje inject that provides a default TemplateRender instance.
 */
public final class DefaultTemplateProvider implements Plugin {

  @Override
  public Class<?>[] provides() {
    return new Class<?>[]{TemplateRender.class};
  }

  @Override
  public void apply(BeanScopeBuilder builder) {
    builder.provideDefault(null, TemplateRender.class, JStacheTemplateRender::new);
  }
}
