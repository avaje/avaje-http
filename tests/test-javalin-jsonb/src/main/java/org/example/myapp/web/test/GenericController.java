package org.example.myapp.web.test;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.jsonb.Json;

@Path("/jsonbGeneric")
@Controller
public class GenericController {

  @Json
  public static class Data<T> {}

  @Json
  public static class Data2<T, T2> {}

  @Get
  Data<String> getData() {
    return null;
  }

  @Get
  Data2<String, ?> getData2() {
    return null;
  }
}
