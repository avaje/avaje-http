module io.avaje.http.client.generator {

  provides javax.annotation.processing.Processor with io.avaje.http.generator.client.ClientProcessor;

  requires java.compiler;
  requires java.sql;

  requires io.avaje.http.generator.core;
  requires static io.avaje.prism;

}
