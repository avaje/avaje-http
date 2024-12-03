package org.example.myapp.web.test;

import java.util.Set;

import org.example.myapp.web.ServerType;

import io.avaje.http.api.BodyString;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Default;
import io.avaje.http.api.ExceptionHandler;
import io.avaje.http.api.Filter;
import io.avaje.http.api.Form;
import io.avaje.http.api.FormParam;
import io.avaje.http.api.Get;
import io.avaje.http.api.InstrumentServerContext;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.QueryParam;
import io.avaje.sigma.HttpContext;
import io.avaje.sigma.HttpFilter.FilterChain;

@Path("test/")
@Controller
public class TestController2 {

  @Form
  @Get("/enumForm")
  @InstrumentServerContext
  void enumForm(String s, ServerType type, HttpContext ctx) {
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
  String exceptionCtx(Exception ex, HttpContext ctx) {

    return "";
  }

  @ExceptionHandler(RuntimeException.class)
  void exceptionVoid(HttpContext ctx) {
    System.err.println("do nothing lmao");
  }

 // @After
  void after(String s, ServerType type, HttpContext ctx) {
    ctx.result(s);
  }

 // @Before
  void before(String s, ServerType type, HttpContext ctx) {
    ctx.result(s);
  }

  @Filter
  void filter(HttpContext ctx, FilterChain chain) {
  }

  @Form
  @Get("/formMulti")
  String formMulti(Set<String> strings) {
    return strings.toString();
  }
}
