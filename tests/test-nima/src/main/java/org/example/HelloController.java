package org.example;

import io.avaje.http.api.BeanParam;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Default;
import io.avaje.http.api.Get;
import io.avaje.http.api.Produces;

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

  @Roles({AppRoles.ADMIN, AppRoles.BASIC_USER})
  @Produces("text/plain")
  @Get("other/{name}")
  String name(String name, String sortBy, @Default("0") long max) {
    return "hi " + name;
  }

  @Get("banana")
  Person getP(@BeanParam Person person) {
    return person;
  }
}
