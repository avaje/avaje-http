module io.avaje.http.generator.core {

  exports io.avaje.http.generator.core;
  exports io.avaje.http.generator.core.javadoc;
  exports io.avaje.http.generator.core.openapi;

  requires java.sql;
  requires java.compiler;
  requires io.swagger.v3.oas.models;
  requires transitive io.avaje.http.generator.prisms;
  requires static io.avaje.prism;
}
