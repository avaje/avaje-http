module io.avaje.http.javalin.generator {

  provides javax.annotation.processing.Processor with io.avaje.http.generator.javalin.JavalinProcessor;

  requires java.compiler;

  // SHADED: All content after this line will be removed at package time
  requires transitive io.avaje.http.generator.core;
}
