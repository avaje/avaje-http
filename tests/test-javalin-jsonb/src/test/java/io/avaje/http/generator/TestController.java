package io.avaje.http.generator;

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
import io.javalin.http.Context;

@Controller
public class TestController {

  @Get
  @Produces(MediaType.TEXT_PLAIN)
  String index() {
    return "Hello world - index";
  }

  @Get("hello")
  @Produces(MediaType.TEXT_PLAIN)
  String helloWorld() {
    return "Hello world";
  }

  @Get("/get")
  @Produces("image/png")
  byte[] testBytes() {
    return "not really an image but ok".getBytes();
  }

  @Get("/ctx")
  void testVoid(Context ctx) {

    ctx.result("success path:" + ctx.path());
  }

  @Get("/header")
  String testHeader(@Header String head) {
    return head;
  }

  @Get("person/{name}")
  Person testParamAndBody(String name) {
    return new Person(42, name + " hello");
  }

  @Post("/person")
  Person testPostPerson(Person body) {
    return new Person(42, "Returning " + body.getName());
  }

  @Get("person/{sortBy}/list")
  List<Person> testPersonList(String sortBy) {
    return List.of(new Person(42, "fooList"), new Person(43, "barList"));
  }

  // curl -v localhost:8081/person/foo/set
  @Get("person/{sortBy}/set")
  Set<Person> testPersonSet(String sortBy) {
    return Set.of(new Person(42, "fooSet"), new Person(43, "barSet"));
  }

  @Get("person/{sortBy}/map")
  Map<String, Person> testPersonMap(String sortBy) {
    return Map.of("one", new Person(42, "fooMap"), "two", new Person(43, "barMap"));
  }

  @Put("person/update")
  String testPersonListBody(List<Person> newGuys) {
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
  String testForm(String name, String email, String url) {
    return name + "-" + email + "-" + url;
  }

  // curl -X POST http://localhost:8081/formBean
  //   -H "Content-Type:application/x-www-form-urlencoded"
  //   -d "name=FormBeanJimmy&email=jim@foo&url=notaurl"
  @Form
  @Post("formBean")
  String testFormBean(MyForm form) {
    return form.name + "|" + form.email + "|" + form.url;
  }
}
