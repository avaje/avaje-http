package org.example.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;

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
}
