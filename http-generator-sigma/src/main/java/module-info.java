module io.avaje.http.sigma.generator {

  provides javax.annotation.processing.Processor with io.avaje.http.generator.sigma.SigmaProcessor;

  requires java.compiler;
  requires java.sql;

  // SHADED: All content after this line will be removed at package time
  requires io.avaje.http.generator.core;
  requires static io.avaje.prism;
  requires static io.avaje.spi;
}
