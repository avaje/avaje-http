package org.example.path;

import org.example.htmx.PathNestController.NestedTypeResponse;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;

@Path("test")
@Controller
public class PathTestController {

  @Produces("text/plain")
  @Get
  String hello() {
    return "hi";
  }

  @Get("/nested/respo")
  NestedTypeResponse nested() {
    return new NestedTypeResponse(0, null);
  }
}
