module io.avaje.http.jex.generator {

  provides javax.annotation.processing.Processor with io.avaje.http.generator.jex.JexProcessor;

  requires java.compiler;
  requires java.sql;

  // SHADED: All content after this line will be removed at package time
  requires transitive io.avaje.http.generator.core;
  requires static io.avaje.prism;
}
