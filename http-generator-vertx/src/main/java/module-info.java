module io.avaje.http.vertx.generator {

  provides javax.annotation.processing.Processor with io.avaje.http.generator.vertx.VertxProcessor;

  requires java.compiler;
  requires java.sql;
  requires static io.avaje.prism;
  requires transitive io.avaje.http.generator.core;
}
