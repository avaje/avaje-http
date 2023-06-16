package org.example.web;

import java.util.Set;

import io.avaje.http.api.*;
import io.avaje.jex.Context;

@Path("test/")
@Controller
@InstrumentServerContext
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

  @Post("/strBody")
  String strBody(@BodyString String body, Context ctx) {
    return body;
  }
}
