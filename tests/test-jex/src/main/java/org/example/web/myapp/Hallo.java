package org.example.web.myapp;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Path;

@Controller
@Path("hallo")
public class Hallo {

  public String getStuff() {
    return "Hallo";
  }
}
