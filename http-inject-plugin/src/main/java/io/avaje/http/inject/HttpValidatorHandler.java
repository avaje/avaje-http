package io.avaje.http.inject;

import io.avaje.http.api.AvajeJavalinPlugin;
import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.InjectPlugin;
import io.avaje.inject.spi.PluginProvides;
import io.avaje.jex.Routing.HttpService;
import io.helidon.webserver.http.HttpFeature;

/** Plugin for avaje inject that provides a default Validator Handler */
@PluginProvides(
    providesStrings = {
      "io.helidon.webserver.http.HttpFeature",
      "io.avaje.http.api.AvajeJavalinPlugin",
      "io.avaje.jex.Routing.HttpService",
    })
public final class HttpValidatorHandler implements InjectPlugin {

  @Override
  public void apply(BeanScopeBuilder builder) {
    try {
      builder.provideDefault(HttpFeature.class, HelidonHandler::new);
    } catch (NoClassDefFoundError e) {
    }
    try {
      builder.provideDefault(AvajeJavalinPlugin.class, JavalinHandler::new);
    } catch (NoClassDefFoundError e) {
    }
    try {
      builder.provideDefault(HttpService.class, JexHandler::new);
    } catch (NoClassDefFoundError e) {
    }
  }
}
