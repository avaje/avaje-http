package org.example;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Cookie;
import io.avaje.http.api.Default;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.QueryParam;

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

  @Get("/mapTest")
  String mapTest(Map<String, List<String>> strings, @Cookie Map<String, List<String>> cookie) {
    return strings.toString();
  }
}
