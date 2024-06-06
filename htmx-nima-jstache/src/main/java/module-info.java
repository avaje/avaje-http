module io.avaje.htmx.nima.jstache {

  exports io.avaje.htmx.nima.jstache;

  requires transitive io.avaje.htmx.nima;
  requires transitive io.helidon.webserver;
  requires transitive io.jstach.jstachio;
  requires io.avaje.inject;

  provides io.avaje.inject.spi.Plugin with io.avaje.htmx.nima.jstache.DefaultTemplateProvider;
}
