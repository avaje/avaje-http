module io.avaje.http.client.otel {

  exports io.avaje.http.client.otel;

  requires transitive io.avaje.http.client;
  requires transitive io.opentelemetry.api;
  requires transitive io.opentelemetry.context;
}
