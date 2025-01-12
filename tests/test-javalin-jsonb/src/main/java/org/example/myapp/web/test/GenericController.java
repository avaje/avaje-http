package org.example.myapp.web.test;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;

@Path("generic/")
@Controller
public class GenericController {

  public interface Data<T> {}

  public interface Data2<T, T2> {}

  @Get
  Data<String> getData() {
    return null;
  }

  @Get
  Data2<String, ?> getData2() {
    return null;
  }
}
