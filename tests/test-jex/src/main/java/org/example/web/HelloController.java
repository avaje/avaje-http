package org.example.web;

import io.avaje.http.api.*;
import io.avaje.jex.Context;

import javax.validation.Valid;

// @Roles(AppRoles.BASIC_USER)
@Controller
@Path("/")
public class HelloController {

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
}
