package org.example;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;

@Controller
public class HelloController {

  @Get("hello")
  String helloWorld() {
    return "Hello world";
  }

  @Get("person/{name}/{sortBy}")
  Person person(String name, String sortBy) {
    var p = new Person();
    p.setId(42);
    p.setName(name + " hello" + " sortBy:" + sortBy);
    return p;
  }
}
