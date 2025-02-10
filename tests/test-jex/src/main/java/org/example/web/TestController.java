package org.example.web;

import java.util.Set;

import io.avaje.http.api.BodyString;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Default;
import io.avaje.http.api.ExceptionHandler;
import io.avaje.http.api.Filter;
import io.avaje.http.api.Get;
import io.avaje.http.api.Options;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.avaje.http.api.QueryParam;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpFilter.FilterChain;

@Path("test/")
@Controller
public class TestController {

  @Get("/paramMulti")
  String paramMulti(Set<String> strings) {
    return strings.toString();
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

  @Options("/strBody")
  String strBody(@BodyString String body, Context ctx) {
    return body;
  }

  @Filter
  void filter(FilterChain chain) {
    // do nothing
    chain.proceed();
  }

  @ExceptionHandler
  String exception(IllegalArgumentException ex) {
    return "Err: " + ex;
  }

  @Produces(statusCode = 501)
  @ExceptionHandler
  HelloDto exceptionCtx(Exception ex, Context ctx) {
    return null;
  }

  @ExceptionHandler(IllegalStateException.class)
  void exceptionVoid(Context ctx) {
    ctx.status(503);
    ctx.text("IllegalStateException");
  }
}
