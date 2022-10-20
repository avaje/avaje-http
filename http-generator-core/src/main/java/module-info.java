module io.avaje.http.generator.core {
  exports io.avaje.http.generator.core;
  exports io.avaje.http.generator.core.javadoc;
  exports io.avaje.http.generator.core.openapi;

  requires java.sql;
  requires java.compiler;
  requires transitive io.avaje.http.api;
  requires transitive io.swagger.v3.oas.models;
  requires transitive io.swagger.v3.oas.annotations;
  requires transitive com.fasterxml.jackson.core;
  requires transitive com.fasterxml.jackson.databind;
  requires transitive com.fasterxml.jackson.annotation;
  requires transitive java.validation;
}
