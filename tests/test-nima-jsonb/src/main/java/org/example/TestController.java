package org.example;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import io.avaje.http.api.*;

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
  @Get(value =  "/inputStream")
  InputStream stream(InputStream stream) throws Exception {
    return stream;
  }


  @Post("/strBody")
  String strBody(@BodyString String body) {
    return body;
  }
}
