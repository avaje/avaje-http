module io.avaje.http.generator.core {

  exports io.avaje.http.generator.core;
  exports io.avaje.http.generator.core.javadoc;
  exports io.avaje.http.generator.core.openapi;

  requires java.sql;
  requires java.compiler;

  // SHADED: All content after this line will be removed at package time
  requires static io.avaje.prism;
  requires static io.avaje.http.api;
  requires static io.swagger.v3.oas.models;
  requires static io.swagger.v3.oas.annotations;
  requires static java.validation;
  requires static jakarta.validation;
  requires static io.avaje.validation.contraints;
}
