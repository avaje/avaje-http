package org.example.web;

import java.math.BigInteger;
import java.util.stream.Stream;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Default;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import io.avaje.http.api.Put;
import io.avaje.http.api.Valid;
import io.avaje.jex.http.Context;

// @Roles(AppRoles.BASIC_USER)
@Controller
@Path("/")
public class HelloController {

  @Get("stream")
  Stream<HelloDto> stream() {
    return Stream.of(new HelloDto(1,"a"), new HelloDto(2, "b"));
  }

  @Get
  HelloDto getHello() {
    HelloDto dto = new HelloDto();
    dto.id = 42;
    dto.name = "rob";
    return dto;
  }

  @Produces("text/plain")
  @Get("plain")
  String getText() {
    return "something";
  }

  @Roles({AppRoles.ADMIN, AppRoles.BASIC_USER})
  @Produces("text/plain")
  @Get("other/{name}")
  String name(String name) {
    return "hi " + name;
  }

  @Produces("text/plain")
  @Get("withDefault/{name}")
  String withDefault(String name, @Default("42") String limit) {
    return "name|" + name+";limit|"+limit;
  }

  @Produces("text/plain")
  @Get("splat/{name}/<s0>/other/<s1>")
  String splat(String name, Context ctx) {
    return "got name:" + name + " splat0:" + ctx.pathParam("s0") + " splat1:" + ctx.pathParam("s1");
  }

  @Produces("text/plain")
  @Get("splat2/{name}/<nam0>/other/<nam1>")
  String splat2(String name, String nam0, String nam1) {
    return "got name:" + name + " splat0:" + nam0 + " splat1:" + nam1;
  }

  @Valid
  @Put
  void put(HelloDto dto) {
    dto.hashCode();
  }

  @Produces("text/plain")
  @Get("/bigInt/{val}")
  String testBigInt(BigInteger val) {
    return "hi|" + val;
  }

  @Get("rawJson")
  String rawJsonString() {
    return "{\"key\": 42 }";
  }
}
