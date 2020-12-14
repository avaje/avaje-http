package org.example.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;

@Controller
@Path("/")
public class HelloController {

  @Get
  HelloDto getHello() {
    HelloDto dto= new HelloDto();
    dto.id = 42;
    dto.name = "rob";
    return dto;
  }

  @Produces("text/plain")
  @Get("plain")
  String getText() {
    return "something";
  }

  @Produces("text/plain")
  @Get("other/{name}")
  String name(String name) {
    return "hi "+name;
  }
}
