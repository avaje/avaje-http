module io.avaje.http.client {

  uses io.avaje.http.client.HttpApiProvider;

  requires transitive java.net.http;
  requires transitive org.slf4j;
  requires static com.fasterxml.jackson.databind;
  requires static com.fasterxml.jackson.annotation;
  requires static com.fasterxml.jackson.core;
  requires static io.avaje.jsonb;

  exports io.avaje.http.client;
}
