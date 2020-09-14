package org.example.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;

@Controller
@Path("hello")
public class HelloController {

  @Produces("text/plain")
  @Get
  String helloThere2() {
    return "helloThere";
  }

  @Get("{id}")
  Hello getById(long id) {
    Hello hello =new Hello();
    hello.id = id;
    hello.name = "foo";
    return hello;
  }
}
