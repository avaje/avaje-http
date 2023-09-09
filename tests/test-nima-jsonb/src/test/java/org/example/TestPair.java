package org.example;

import io.avaje.http.api.context.ThreadLocalRequestContextResolver;
import io.avaje.http.client.HttpClient;
import io.avaje.http.hibernate.validator.BeanValidator;
import io.avaje.jsonb.Jsonb;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;

public class TestPair {

  WebServer webServer;
  HttpClient httpClient;
  int port;

  public TestPair() {
    this.webServer = WebServer.builder()
      .routing(routing().build())
      .build();

    webServer.start();
    this.port = webServer.port();

    this.httpClient = HttpClient.builder()
      .baseUrl("http://localhost:" + port)
      .build();
  }

  public HttpClient client() {
    return httpClient;
  }

  void stop() {
    webServer.stop();
  }

  private static HttpRouting.Builder routing() {
    HttpRouting.Builder routing = HttpRouting.builder();

    var beanValidator = new BeanValidator();
    Jsonb jsonb = Jsonb.builder().build();

    var ec = new ErrorController();
    var ecRoute = new ErrorController$Route(ec, jsonb);
    routing.addFeature(ecRoute);

    var hc = new HelloController();
    var hello = new HelloController$Route(hc, beanValidator, jsonb);
    routing.addFeature(hello);

    var cr = new ThreadLocalRequestContextResolver();
    var tc = new TestController();
    TestController$Route tcr = new TestController$Route(tc, jsonb, cr);

    routing.addFeature(tcr);
    return routing;
  }
}
