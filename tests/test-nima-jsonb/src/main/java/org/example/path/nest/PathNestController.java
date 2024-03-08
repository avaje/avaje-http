package org.example.path.nest;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import io.avaje.jsonb.Json;

@Path("test")
@Controller
public class PathNestController {
  @Json
  public record NestedTypeResponse(long id, String name) {}

  @Produces("text/plain")
  @Get
  String hello() {
    return "hi";
  }
}
