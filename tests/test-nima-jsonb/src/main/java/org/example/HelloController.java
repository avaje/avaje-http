package org.example;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;

@Controller
public class HelloController {

  @Produces("image/png")
  @Get("/get")
  byte[] testBytes() {

    return "not really an image but ok".getBytes();
  }

  @Get("hello")
  String helloWorld() {
    return "Hello world";
  }

  @Get("person/{name}/{sortBy}")
  Person person(String name, String sortBy) {
    final var p = new Person();
    p.setId(42);
    p.setName(name + " hello" + " sortBy:" + sortBy);
    return p;
  }

  @Post("person/update")
  String add(Person newGuy) {

    return "New Guy Added";
  }
}
