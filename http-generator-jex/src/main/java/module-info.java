module io.avaje.http.jex.generator {

  provides javax.annotation.processing.Processor with io.avaje.http.generator.jex.JexProcessor;

  requires java.compiler;
  requires java.sql;

  requires transitive io.avaje.http.generator.core;
  requires static io.avaje.prism;
}
