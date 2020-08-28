package org.example.myapp.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;

import java.util.Arrays;
import java.util.List;

@Controller
@Path("/baz")
class BazController extends BaseController<Baz, Long> {

  BazController(Repository<Baz, Long> repository) {
    super(repository);
  }

  /**
   * Find the baz by name.
   * <p>
   * This is some more comments about this method.
   *
   * @return The list of baz
   */
  @Get("findbyname/{name}")
  List<Baz> searchByName(String name) {

    Baz b1 = new Baz();
    b1.id = 1L;
    b1.name = "baz1-" + name;

    Baz b2 = new Baz();
    b2.id = 2L;
    b2.name = "baz2";

    return Arrays.asList(b1, b2);
  }
}
