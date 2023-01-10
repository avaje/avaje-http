import io.avaje.inject.spi.Module;

module io.avaje.nima {

  exports io.avaje.nima;

  requires io.avaje.jsonb;
  requires io.avaje.inject;
  requires io.helidon.nima.webserver;

  provides Module with io.avaje.nima.config.NimaDefaultsModule;

}
