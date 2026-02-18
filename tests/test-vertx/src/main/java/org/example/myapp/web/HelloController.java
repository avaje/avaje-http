package org.example.myapp.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Produces;

@Controller
public class HelloController {

  @Produces("text/plain")
  @Get("hello")
  String helloWorld() {
    return "hello world";
  }
}
