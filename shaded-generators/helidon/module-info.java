module io.avaje.http.client.helidon {

  provides javax.annotation.processing.Processor with io.avaje.http.generator.helidon.nima.HelidonProcessor;

  requires java.compiler;
  requires java.sql;
}
