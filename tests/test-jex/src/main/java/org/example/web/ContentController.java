package org.example.web;

import io.avaje.htmx.api.Html;
import io.avaje.htmx.api.HxRequest;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import org.example.web.template.ViewHome;
import org.example.web.template.ViewPartial;

@Controller
@Html
@Path("ui")
public class ContentController {

  @HxRequest
  @Get
  ViewPartial indexHxPartial() {
    return new ViewPartial("Rob");
  }

  @Get
  ViewHome index() {
    return new ViewHome("Hi");
  }

}
