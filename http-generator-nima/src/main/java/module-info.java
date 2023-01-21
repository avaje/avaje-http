module io.avaje.http.nima.generator {

  provides javax.annotation.processing.Processor with io.avaje.http.generator.helidon.nima.NimaProcessor;

  requires transitive io.avaje.http.generator.core;
  requires java.compiler;
}
