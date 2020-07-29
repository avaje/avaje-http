package org.example.myapp.web;

import io.dinject.controller.Controller;
import io.dinject.controller.Path;

@Controller
@Path("hallo")
public class Hallo {

  public String getStuff() {
    return "Hallo";
  }
}
