package org.example;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.jsonb.Json;

@Path("/foo")
@Controller
public class FooController {

  @Get
  Foo one() {
    return new Foo(82, "Foo here");
  }

  @Json
  public record Foo(int id, String name) {

  }
}
