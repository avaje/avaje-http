package org.example.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import io.avaje.jex.Context;

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
  @Get("splat/{name}/*/other/*")
  String splat(String name, Context ctx) {
    return "got name:" + name + " splat0:" + ctx.splat(0) + " splat1:" + ctx.splat(1);
  }
}
