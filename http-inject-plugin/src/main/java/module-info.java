import io.avaje.http.inject.DefaultResolverProvider;
import io.avaje.http.inject.HttpValidatorHandler;

module io.avaje.http.plugin {

  requires io.avaje.http.api;
  requires io.avaje.inject;
  requires static io.avaje.jex;
  requires static io.javalin;
  requires static io.helidon.webserver;
  provides io.avaje.inject.spi.InjectExtension with DefaultResolverProvider, HttpValidatorHandler;
}
