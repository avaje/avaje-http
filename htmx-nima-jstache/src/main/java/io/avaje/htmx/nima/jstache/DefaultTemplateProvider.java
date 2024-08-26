package io.avaje.htmx.nima.jstache;

import io.avaje.htmx.nima.TemplateContentCache;
import io.avaje.htmx.nima.TemplateRender;
import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.InjectPlugin;
import io.avaje.spi.ServiceProvider;

/**
 * Plugin for avaje inject that provides a default TemplateRender instance.
 */
@ServiceProvider
public final class DefaultTemplateProvider implements InjectPlugin {

  @Override
  public Class<?>[] provides() {
    return new Class<?>[]{TemplateRender.class, TemplateContentCache.class};
  }

  @Override
  public void apply(BeanScopeBuilder builder) {
    builder.provideDefault(null, TemplateRender.class, JStacheTemplateRender::new);
    builder.provideDefault(null, TemplateContentCache.class, SimpleContentCache::new);
  }
}
