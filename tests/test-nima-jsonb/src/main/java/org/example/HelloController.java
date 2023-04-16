package org.example;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Form;
import io.avaje.http.api.Get;
import io.avaje.http.api.Header;
import io.avaje.http.api.MediaType;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.avaje.http.api.Put;
import io.avaje.http.api.QueryParam;
import io.avaje.http.api.Valid;
import io.helidon.common.http.HttpMediaType;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.nima.webserver.http.ServerResponse;

@Controller
public class HelloController {

  @Produces(MediaType.TEXT_PLAIN)
  @Get
  String index() {
    return "Hello world - index";
  }

  @Produces(MediaType.TEXT_PLAIN)
  @Get("hello")
  String helloWorld() {
    return "Hello world";
  }

  @Produces("image/png")
  @Get("/get")
  byte[] testBytes() {
    return "not really an image but ok".getBytes();
  }

  @Get("/helidon")
  void testHelidon(ServerRequest req, ServerResponse res) {

    res.headers().contentType(HttpMediaType.TEXT_PLAIN);
    res.send("success path:" + req.path());
  }

  @Get("/void")
  void testVoid(ServerResponse res) {
    res.send("GET-Returning-void");
  }

  @Get("/header")
  String testHeader(@Header String head) {
    return head;
  }

  @Get("/param")
  String testParam(@QueryParam String param) {
    return param;
  }

  // curl -v localhost:8081/person/jack
  @Get("person/{name}")
  Person person(String name) {
    return new Person(42, name + " hello");
  }

  @Get("person/{id}")
  Person testLong(long id) {
    return new Person(id, "Giorno hello");
  }

  // curl -X POST http://localhost:8081/person -H 'Content-Type: application/json' -d
  // '{"id":942,"name":"Jimmy"}'
  @Post("/person")
  Person postPerson(Person body) {
    return new Person(42, "Returning " + body.name());
  }

  // curl -v localhost:8081/person/foo/list
  @Get("person/{sortBy}/list")
  List<Person> personList(String sortBy) {
    return List.of(new Person(42, "fooList"), new Person(43, "barList"));
  }

  // curl -v localhost:8081/person/foo/set
  @Get("person/{sortBy}/set")
  Set<Person> personSet(String sortBy) {
    return Set.of(new Person(42, "fooSet"), new Person(43, "barSet"));
  }

  @Get("person/{sortBy}/map")
  Map<String, Person> personMap(String sortBy) {
    return Map.of("one", new Person(42, "fooMap"), "two", new Person(43, "barMap"));
  }

  @Post("person/update")
  String add(Person newGuy) {
    return "New Guy Added - " + newGuy;
  }

  @Put("person/update")
  String addMultiple(List<Person> newGuys) {
    return "New Guys Added";
  }

  @Put("test/int")
  int testIntReturn() {
    return 422;
  }

  @Put("test/long")
  long testLongReturn() {
    return 69;
  }

  // curl -X POST http://localhost:8081/form
  //   -H "Content-Type: application/x-www-form-urlencoded"
  //   -d "name=Jimmy&email=jim@foo&url=notaurl"
  @Form
  @Post("form")
  String form(String name, String email, String url) {
    return name + "-" + email + "-" + url;
  }

  // curl -X POST http://localhost:8081/formBean
  //   -H "Content-Type:application/x-www-form-urlencoded"
  //   -d "name=FormBeanJimmy&email=jim@foo&url=notaurl"
  @Form
  @Post("formBean")
  @Valid
  String formBean(MyForm form) {
    return form.name + "|" + form.email + "|" + form.url;
  }
}
