module io.avaje.http.javalin.generator {

  provides javax.annotation.processing.Processor with io.avaje.http.generator.javalin.JavalinProcessor;

  requires java.compiler;
  requires java.sql;

  // SHADED: All content after this line will be removed at package time
  requires io.avaje.http.generator.core;
  requires static io.avaje.http.api.javalin;
  requires static io.avaje.prism;
}
