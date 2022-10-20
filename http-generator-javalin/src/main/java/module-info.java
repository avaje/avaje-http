module io.avaje.http.javalin.generator {
  provides javax.annotation.processing.Processor with
      io.avaje.http.generator.javalin.JavalinProcessor;

  requires transitive io.avaje.http.generator.core;
  requires java.compiler;
}
