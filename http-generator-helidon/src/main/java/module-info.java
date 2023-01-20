module io.avaje.http.helidon.generator {

  provides javax.annotation.processing.Processor with io.avaje.http.generator.helidon.HelidonProcessor;

  requires transitive io.avaje.http.generator.core;
  requires java.compiler;
}
