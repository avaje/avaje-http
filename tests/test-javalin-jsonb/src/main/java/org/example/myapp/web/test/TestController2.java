package org.example.myapp.web.test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.example.myapp.web.ServerType;

import io.avaje.http.api.BodyString;
import io.avaje.http.api.Consumes;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Default;
import io.avaje.http.api.Form;
import io.avaje.http.api.FormParam;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.QueryParam;
import io.javalin.http.Context;

@Path("test/")
@Controller
public class TestController2 {

  @Form
  @Get(value = "/enumForm", instrumentRequestContext = true)
  void enumForm(String s, ServerType type, Context ctx) {
    ctx.result(s);
  }

  @Get(value = "/enumFormParam", instrumentRequestContext = true)
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
  String enumQueryImplied(ServerType type) {
    return type.name();
  }

  @Get("/mapTest")
  String mapTest(Map<String, List<String>> strings) {
    return strings.toString();
  }

  @Get("/inputStream")
  @Consumes("application/bson")
  String stream(InputStream stream) {
    return stream.toString();
  }

  @Get("/byteArray")
  String bytes(byte[] array) {
    return array.toString();
  }

  @Post(value = "/strBody", instrumentRequestContext = true)
  String strBody(@BodyString String body) {
    return body;
  }
}
