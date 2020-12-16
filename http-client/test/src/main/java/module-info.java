import example.github.SimpleHttpClient;

open module test {

  requires io.avaje.http.client;
  requires com.fasterxml.jackson.databind;
  requires com.google.gson;

  provides io.avaje.http.client.HttpApiProvider with SimpleHttpClient;

  exports example.github;
}
