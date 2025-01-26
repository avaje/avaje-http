open module test {

  requires io.avaje.http.client;
  requires io.avaje.http.api;
  requires com.fasterxml.jackson.databind;
  requires com.google.gson;

  exports example.github;

  provides io.avaje.http.client.HttpClient.GeneratedComponent
      with example.github.httpclient.GeneratedHttpComponent, example.github.pkgprivate.PkgprivateHttpComponent;
}
