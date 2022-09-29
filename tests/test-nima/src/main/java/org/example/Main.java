package org.example;

import io.avaje.inject.BeanScope;
import io.helidon.nima.webserver.Routing;
import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.http.HttpRouting;
import io.helidon.nima.webserver.http.HttpService;

import java.util.List;

public class Main {

  public static void main(String[] args) {

    BeanScope scope = BeanScope.builder().build();
    List<HttpService> list = scope.list(HttpService.class);
    HttpRouting.Builder builder = HttpRouting.builder();
    for (HttpService httpService : list) {
      httpService.routing(builder);
    }
    HttpRouting httpRouting = builder.build();


    WebServer.builder()
      .addRouting(httpRouting)
      //.routing(Main::routing)
      .port(8081)
      .start();

    System.out.println("started");
  }

  private static Routing routing(HttpRouting.Builder route) {
    return route
      .get("/", (req, res) -> {
        res.send("hello world");
      })
      .get("/hi", (req, res) -> {
        res.header("my-header", "hi-header");
        //res.send("hi!!");

        var p = new Person();
        p.setId(42);
        p.setName("asdasd");

        //
        res.send(p);
      })
      .build();
  }
}
