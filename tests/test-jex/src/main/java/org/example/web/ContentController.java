package org.example.web;

import io.avaje.htmx.api.Html;
import io.avaje.htmx.api.HxRequest;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import org.example.web.template.ViewHome;

@Controller
@Html
@Path("ui")
public class ContentController {

  @Get
  Object index() {
    return  new ViewHome("Hi");
  }

  @HxRequest
  @Get
  Object indexHxReqest() {
    return  new ViewHome("Hi");
  }

}
