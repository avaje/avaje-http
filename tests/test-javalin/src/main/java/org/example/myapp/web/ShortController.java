package org.example.myapp.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;

import org.jspecify.annotations.NullMarked;

@NullMarked
@Controller
@Path("/shorttest")
public class ShortController {

  @Get()
  public ShortDto get(short shortValue, Short shortObjectValue) {
    return new ShortDto(shortValue, shortObjectValue);
  }
}
