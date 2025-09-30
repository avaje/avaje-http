package org.example;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.avaje.http.api.BodyString;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Default;
import io.avaje.http.api.ExceptionHandler;
import io.avaje.http.api.Filter;
import io.avaje.http.api.Form;
import io.avaje.http.api.FormParam;
import io.avaje.http.api.Get;
import io.avaje.http.api.InstrumentServerContext;
import io.avaje.http.api.MappedParam;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.avaje.http.api.QueryParam;
import io.avaje.http.api.Valid;
import io.helidon.webserver.http.FilterChain;
import io.helidon.webserver.http.RoutingResponse;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

@Path("test")
@Controller
public class TestController {

  @Produces("text/plain")
  @Get
  String hello() {
    return "hi";
  }

  @Get("/paramMulti")
  String paramMulti(Set<String> strings) {
    return strings.toString();
  }

  @Form
  @Get("/formMulti")
  String formMulti(Set<String> strings) {
    return strings.toString();
  }

  @Form
  @Get("/formMap")
  String formMap(Map<String, List<String>> strings) {
    return strings.toString();
  }

  @Get("/BoxCollection")
  String boxed(@QueryParam List<Long> l) {
    return l.toString();
  }

  @Form
  @Get("/enumForm")
  String enumForm(String s, ServerType type) {
    return type.name();
  }

  @Get("/enumFormParam")
  String enumFormParam(@FormParam String s, @FormParam ServerType type) {
    return type.name();
  }

  @Get("/enumQuery")
  String enumQuery(@QueryParam @Default("FFA") ServerType type) {
    return type.name();
  }

  @Get("/enumQuery2")
  String enumMultiQuery(@QueryParam @Default({"FFA", "PROXY"}) Set<ServerType> type) {
    return type.toString();
  }

  @Post("/enumQueryImplied")
  String enumQueryImplied(String s, @QueryParam ServerType type) {
    return type.name();
  }

  @InstrumentServerContext
  @Post(value = "/inputStream")
  InputStream stream(InputStream stream) throws Exception {
    return stream;
  }

  @Post("/strBody")
  String strBody(@BodyString String body) {
    return body;
  }

  @Produces(statusCode = 202)
  @Post("/blah")
  Map<String, Object> strBody2() {
    var map = new LinkedHashMap<String, Object>();
    map.put("hi", "yo");
    map.put("level", 42L);
    return map;
  }

  @Produces(statusCode = -1) // Can use -1 to programmatically set the ServerResponse statusCode
  @Post("/strBody3")
  Map<String, Object> strBody3(ServerResponse res) {
    res.status(200);
    return Map.of("hi", "strBody3");
  }

  @ExceptionHandler
  String exception(IllegalArgumentException ex) {
    return "Err: " + ex;
  }

  @Produces(statusCode = 501)
  @ExceptionHandler
  Person exceptionCtx(Exception ex, ServerRequest req, ServerResponse res) {
    res.header("X-Foo", "WasHere");
    return new Person(0, null);
  }

  @ExceptionHandler(IllegalStateException.class)
  void exceptionVoid(ServerResponse res) {
    res.status(503);
    res.send("IllegalStateException");
  }

  @Filter
  void filter(FilterChain chain, RoutingResponse res) {
    System.err.println("do nothing lmao");
    chain.proceed();
  }

  @Form
  @Valid(groups = MyForm.class)
  @Post("formBean2")
  String formBean(MyForm form) {
    return form.name + "|" + form.email + "|" + form.url;
  }

  @Get("maybeNoContent")
  String maybeNoContent(Boolean empty) {
    return Boolean.TRUE.equals(empty) ? null : "Hi";
  }

  @Get("/peppermint/{patty:\\d+}")
  String pattern(String patty) {
    return patty.toString();
  }

  @Get("/minus/{+plus}")
  String patternPlus(String plus) {
    return plus.toString();
  }

  @Get("/maybe/{maybe}")
  Person maybePerson(boolean maybe) {
    return maybe ? new Person(9, "hi") : null;
  }

  @Get("/maybeList/{maybe}")
  List<Person> maybePersonList(boolean maybe) {
    return maybe ? List.of(new Person(9, "hi")) : null; // Collections.emptyList();
  }

  @MappedParam
  @MappedParam.Import(Simple2.class)
  record Simple(String name) {}

  record Simple2(String name) {}

  @Form
  @Get("/typeForm")
  String typeForm(Simple s, Simple2 type) {
    return type.name();
  }

  @MappedParam(factoryMethod = "build")
  record Static(String name) {
    static Static build(String name) {
      return null;
    }
  }

  @Get("/typeFormParam")
  String typeFormParam(@FormParam String s, @FormParam Static type) {
    return type.name();
  }

  @Get("/typeQuery")
  String typeQuery(@QueryParam @Default("FFA") Static type) {
    return type.name();
  }

  @Get("/typeQuery2")
  String typeMultiQuery(@QueryParam @Default({"FFA", "PROXY"}) Set<Simple> type) {
    return type.toString();
  }

  record Implied(String name) {}

  @Post("/typeQueryImplied")
  String typeQueryImplied(String s, @QueryParam Implied type) {
    return type.name();
  }
}
