module io.avaje.http.nima.generator {

  exports io.avaje.http.helidon;

  requires java.compiler;
  requires java.sql;

  provides javax.annotation.processing.Processor with io.avaje.http.generator.helidon.nima.NimaProcessor;

  // SHADED: All content after this line will be removed at package time
  requires transitive io.avaje.http.generator.core;
  requires io.avaje.prism;
}
