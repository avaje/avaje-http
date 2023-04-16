package io.avaje.http.api.spi;

import io.avaje.http.api.context.HttpRequestContextResolver;
import io.avaje.http.api.context.ThreadLocalResolver;
import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.Plugin;

/** Plugin for avaje inject that provides a default HttpRequestContextResolver instance. */
public final class DefaultResolverProvider implements Plugin {

  @Override
  public Class<?>[] provides() {
    return new Class<?>[] {HttpRequestContextResolver.class};
  }

  @Override
  public void apply(BeanScopeBuilder builder) {
    builder.provideDefault(null, HttpRequestContextResolver.class, ThreadLocalResolver::new);
  }
}
