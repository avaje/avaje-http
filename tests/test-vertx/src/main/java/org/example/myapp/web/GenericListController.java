package org.example.myapp.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import java.util.List;

@Path("/generic-list")
@Controller
class GenericListController {

  static class Bar {
    String name;
  }

  static class Data<T> {
    List<T> data;
  }

  @Get
  Data<Bar> get() {
    return null;
  }
}
