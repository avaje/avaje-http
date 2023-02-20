module io.avaje.http.generator.core {

  exports io.avaje.http.generator.core;
  exports io.avaje.http.generator.core.javadoc;
  exports io.avaje.http.generator.core.openapi;

  requires java.sql;
  requires java.compiler;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;

  // SHADED: All content after this line will be removed at package time
  requires static io.avaje.prism;
  requires static transitive io.avaje.http.api;
  requires static transitive io.swagger.v3.oas.models;
  requires static transitive io.swagger.v3.oas.annotations;
  requires static transitive java.validation;
  requires static transitive jakarta.inject;
  requires static transitive jakarta.validation;
}
