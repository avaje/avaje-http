package org.example.myapp.web.test;

import java.util.List;
import java.util.Map;

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

  @Get("single")
  Data<String> getData() {
    return null;
  }

  @Get("double")
  Data2<String, ?> getData2() {
    return null;
  }
  @Get("nested")
  Data2<String, Data<String>> getDataNested() {
    return null;
  }

  @Get("nestedMap")
  Map<String, List<String>> getMapDataNested() {
    return null;
  }
}
