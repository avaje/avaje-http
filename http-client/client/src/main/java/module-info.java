module io.avaje.http.client {

  uses io.avaje.http.client.HttpApiProvider;

  requires transitive java.net.http;
  requires transitive org.slf4j;
  requires static com.fasterxml.jackson.databind;

  exports io.avaje.http.client;
}
