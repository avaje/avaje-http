package io.avaje.http.inject;

import io.avaje.http.api.AvajeJavalinPlugin;
import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.InjectPlugin;
import io.avaje.inject.spi.PluginProvides;
import io.avaje.jex.Routing.HttpService;
import io.avaje.spi.ServiceProvider;
import io.helidon.webserver.http.HttpFeature;

/** Plugin for avaje inject that provides a default Validator Handler */
@ServiceProvider
@PluginProvides(
    providesStrings = {
      "io.helidon.webserver.http.HttpFeature",
      "io.avaje.http.api.AvajeJavalinPlugin",
      "io.avaje.jex.Routing.HttpService",
    })
public final class HttpValidatorHandler implements InjectPlugin {

  enum Server {
    HELIDON("io.helidon.webserver.http.HttpFeature"),
    JAVALIN("io.javalin.plugin.Plugin"),
    JEX("io.avaje.jex.Routing.HttpService");

    String register;

    Server(String register) {
      this.register = register;
    }
  }

  private static final Server type = server();

  private static Server server() {

    for (var register : Server.values()) {

      try {
        Class.forName(register.register);
        return register;
      } catch (ClassNotFoundException e) {
        continue;
      }
    }

    return null;
  }

  @Override
  public void apply(BeanScopeBuilder builder) {
    if (type == null) {
      return;
    }
    switch (type) {
      case HELIDON:
        builder.bean(HttpFeature.class, new HelidonHandler());
        break;
      case JAVALIN:
        builder.bean(AvajeJavalinPlugin.class, new JavalinHandler());
        break;
      case JEX:
        builder.bean(HttpService.class, new JexHandler());
        break;
    }
  }
}
