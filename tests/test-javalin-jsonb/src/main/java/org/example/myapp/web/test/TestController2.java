package org.example.myapp.web.test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.example.myapp.web.ServerType;

import io.avaje.http.api.*;
import io.avaje.http.api.javalin.After;
import io.avaje.http.api.javalin.Before;
import io.javalin.http.Context;

@Path("test/")
@Controller
public class TestController2 {

  @Form
  @Get("/enumForm")
  @InstrumentServerContext
  void enumForm(String s, ServerType type, Context ctx) {
    ctx.result(s);
  }

  @Get("/enumFormParam")
  @InstrumentServerContext
  String enumFormParam(@FormParam String s, @FormParam ServerType type) throws Exception {
    return type.name();
  }

  @Get("/enumQuery")
  String enumQuery(@QueryParam @Default("FFA") ServerType type) {
    return type.name();
  }

  @Post("/enumQueryImplied")
  String enumQueryImplied(String s, @QueryParam ServerType type) {
    return type.name();
  }

  @Post("/enumPath/{type}")
  String enumPath(ServerType type) {
    return type.name();
  }

  @Get("/mapTest")
  String mapTest(Map<String, List<String>> strings) {
    return strings.toString();
  }

  @Post("/inputStream")
  @Consumes("application/bson")
  String stream(InputStream stream) {
    return stream.toString();
  }

  @Post("/byteArray")
  String bytes(byte[] array) {
    return array.toString();
  }

  @Post("/strBody")
  @InstrumentServerContext
  String strBody(@BodyString String body) {
    return body;
  }

  @ExceptionHandler
  String exception(Exception ex) {

    return "";
  }

  @ExceptionHandler
  String exceptionCtx(Exception ex, Context ctx) {

    return "";
  }

  @ExceptionHandler(RuntimeException.class)
  void exceptionVoid(Context ctx) {
    System.err.println("do nothing lmao");
  }

  @After
  void after(String s, ServerType type, Context ctx) {
    ctx.result(s);
  }

  @Before
  void before(String s, ServerType type, Context ctx) {
    ctx.result(s);
  }

  @Filter
  void filter(Context ctx) {
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
}
