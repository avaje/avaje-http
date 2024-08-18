package org.example.htmx;

import io.avaje.htmx.api.Html;
import io.avaje.htmx.api.ContentCache;
import io.avaje.htmx.api.HxRequest;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;

import java.time.Instant;
import java.util.List;

@Html
@Controller
@Path("/")
public class UIController {

  @ContentCache
  @Get
  ViewHome index() {
    return new ViewHome("Robin3");
  }

  @ContentCache
  @HxRequest(target = "name")
  @Get("name")
  ViewName name() {
    var mlist = List.of("one","two","three", "four");
    return new ViewName("JimBolin", Instant.now(), "MoreMeMore", mlist);
  }

}
