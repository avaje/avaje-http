package io.avaje.http.inject;

import io.avaje.http.api.AvajeJavalinPlugin;
import io.avaje.http.api.vertx.VertxRouteSet;
import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.InjectPlugin;
import io.avaje.inject.spi.PluginProvides;
import io.avaje.jex.Routing.HttpService;
import io.helidon.webserver.http.HttpFeature;

/**
 * Plugin for avaje inject that provides a default Validator Handler
 */
@PluginProvides(
  providesStrings = {
    "io.helidon.webserver.http.HttpFeature",
    "io.avaje.http.api.AvajeJavalinPlugin",
    "io.avaje.jex.Routing.HttpService",
    "io.avaje.http.api.vertx.VertxRouteSet",
  })
public final class HttpValidatorErrorPlugin implements InjectPlugin {

  @Override
  public void apply(BeanScopeBuilder builder) {
    ModuleLayer bootLayer = ModuleLayer.boot();

    bootLayer.findModule("io.avaje.http.plugin")
      .ifPresentOrElse(m -> {
          if (bootLayer.findModule("io.avaje.jex").isPresent()) {
            builder.provideDefault(HttpService.class, JexHandler::new);
          } else if (bootLayer.findModule("io.helidon.webserver").isPresent()) {
            builder.provideDefault(HttpFeature.class, HelidonHandler::new);
          } else if (bootLayer.findModule("io.javalin").isPresent()) {
            builder.provideDefault(AvajeJavalinPlugin.class, JavalinHandler::new);
          } else if (bootLayer.findModule("io.avaje.http.api.vertx").isPresent()
            && bootLayer.findModule("io.vertx.web").isPresent()) {
            builder.provideDefault(VertxRouteSet.class, VertxHandler::new);
          }
        },
        () -> {
          try {
            builder.provideDefault(HttpService.class, JexHandler::new);
            return;
          } catch (NoClassDefFoundError e) {
            // not present
          }
          try {
            builder.provideDefault(HttpFeature.class, HelidonHandler::new);
            return;
          } catch (NoClassDefFoundError e) {
            // not present
          }
          try {
            builder.provideDefault(AvajeJavalinPlugin.class, JavalinHandler::new);
          } catch (NoClassDefFoundError e) {
            // not present
          }
          try {
            builder.provideDefault(VertxRouteSet.class, VertxHandler::new);
          } catch (NoClassDefFoundError e) {
            // not present
          }
        });
  }
}
