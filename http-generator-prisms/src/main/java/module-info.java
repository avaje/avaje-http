module io.avaje.http.generator.prisms {
  exports io.avaje.http.generator.prisms;

  requires java.compiler;
  requires static io.avaje.htmx.api;
  requires static io.avaje.http.api.javalin;
  requires static io.avaje.http.api.vertx;
  requires static io.avaje.http.api;
  requires static io.avaje.prism;
  requires static io.avaje.validation.contraints;
  requires static io.swagger.v3.oas.annotations;
  requires static io.swagger.v3.oas.models;
  requires static jakarta.validation;
  requires static java.validation;
  requires static org.jspecify;
}
