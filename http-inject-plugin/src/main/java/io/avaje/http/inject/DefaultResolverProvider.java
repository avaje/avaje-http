package io.avaje.http.inject;

import io.avaje.http.api.context.RequestContextResolver;
import io.avaje.http.api.context.ThreadLocalRequestContextResolver;
import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.InjectPlugin;
import io.avaje.inject.spi.PluginProvides;

/** Plugin for avaje inject that provides a default RequestContextResolver instance. */
@PluginProvides
public final class DefaultResolverProvider implements InjectPlugin {

  @Override
  public Class<?>[] provides() {
    return new Class<?>[] {RequestContextResolver.class};
  }

  @Override
  public void apply(BeanScopeBuilder builder) {
    builder.provideDefault(null, RequestContextResolver.class, ThreadLocalRequestContextResolver::new);
  }
}
