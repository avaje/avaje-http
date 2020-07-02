package org.example;

import io.dinject.controller.Controller;
import io.dinject.controller.Get;
import io.dinject.controller.Path;
import io.dinject.controller.Produces;

@Controller
@Path("/foo")
public class FooController {

  //@Produces("text/plain")
  @Get
  public String hello() {
    return "Hello from Foo";
  }

  @Get("{name}")
  public Foo getOne(String name) {
    Foo foo = new Foo();
    foo.name = name;
    foo.age = 42;
    return foo;
  }

  public static class Foo {
    public String name;
    public int age;
  }
}
