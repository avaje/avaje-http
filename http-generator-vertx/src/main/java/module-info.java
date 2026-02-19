module io.avaje.http.vertx.generator {

  provides javax.annotation.processing.Processor with io.avaje.http.generator.vertx.VertxProcessor;

  requires java.compiler;
  requires java.sql;
  requires static io.avaje.http.api.vertx;
  requires static io.avaje.prism;

  // SHADED: All content after this line will be removed at package time
  requires transitive io.avaje.http.generator.core;
}
