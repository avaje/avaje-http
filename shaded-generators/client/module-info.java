module io.avaje.http.client.generator {

  provides javax.annotation.processing.Processor with io.avaje.http.generator.client.ClientProcessor;

  requires java.compiler;
  requires java.sql;
}
