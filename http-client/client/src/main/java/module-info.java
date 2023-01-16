module io.avaje.http.client {

  uses io.avaje.http.client.HttpApiProvider;

  requires transitive java.net.http;
  requires transitive io.avaje.applog;
  requires static com.fasterxml.jackson.databind;
  requires static com.fasterxml.jackson.annotation;
  requires static com.fasterxml.jackson.core;
  requires static io.avaje.jsonb;
  requires static io.avaje.inject;

  exports io.avaje.http.client;
}
