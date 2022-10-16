package org.example;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.avaje.http.api.Put;
import io.helidon.nima.webserver.http.ServerResponse;

@Controller
public class HelloController {

  @Produces("image/png")
  @Get("/get")
  byte[] testBytes() {

    return "not really an image but ok".getBytes();
  }

  @Get("/void")
  void testVoid(Person p, ServerResponse res) {
    res.send("success");
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

  @Get("person/{sortBy}/list")
  List<Person> personList(String sortBy) {
    final var p = new Person();
    p.setId(42);
    return List.of(p, p);
  }

  @Get("person/{sortBy}/set")
  Set<Person> personSet(String sortBy) {
    final var p = new Person();
    p.setId(42);
    return Set.of(p, p);
  }

  @Get("person/{sortBy}/map")
  Map<String, Person> personMap(String sortBy) {
    final var p = new Person();
    p.setId(42);
    return Map.of(sortBy, p);
  }

  @Post("person/update")
  String add(Person newGuy) {

    return "New Guy Added";
  }

  @Put("person/update")
  String addMultiple(List<Person> newGuys) {

    return "New Guys Added";
  }
}
