package org.example;

import io.avaje.http.api.*;
import io.helidon.nima.webserver.http.FilterChain;
import io.helidon.nima.webserver.http.RoutingResponse;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.nima.webserver.http.ServerResponse;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  @Get(value = "/inputStream")
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
}
