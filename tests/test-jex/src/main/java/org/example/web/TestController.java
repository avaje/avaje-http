package org.example.web;

import java.util.Set;

import io.avaje.http.api.BodyString;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Default;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.QueryParam;
import io.avaje.jex.Context;

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

  @Post("/strBody")
  String strBody(@Body String body, Context ctx) {
    return body;
  }
}
