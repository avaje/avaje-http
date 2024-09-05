package org.example.myapp.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Path;

@Controller
@Path("hallo")
public class Hallo {

  public String getStuff() {
    return "Hallo";
  }
}
