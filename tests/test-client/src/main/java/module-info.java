open module test {

  requires io.avaje.http.client;
  requires com.fasterxml.jackson.databind;
  requires com.google.gson;

  exports example.github;

  provides io.avaje.http.client.HttpClient.GeneratedComponent with example.github.httpclient.GeneratedHttpComponent;
}
