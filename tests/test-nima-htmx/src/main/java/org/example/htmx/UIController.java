package org.example.htmx;

import io.avaje.htmx.api.HxRequest;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import io.jstach.jstache.JStache;
import io.jstach.jstachio.JStachio;
import org.example.htmx.model.Name;

import java.time.Instant;
import java.util.List;

@Controller
@Path("/")
@Produces("text/html")
public class UIController {

  @Get
  String index() {
    return JStachio.render(new Home("Robin2"));
  }

  @HxRequest(target = "name")
  @Get("name")
  String name() {
    var mlist = List.of("one","two","three");

//    var mlist = List.of(
//      new Name.Pair("one",23),
//      new Name.Pair("two",34),
//      new Name.Pair("three",43)
//    );
//
//    return "<b>Yo " + Instant.now() + "</b>";
    return JStachio.render(new Name("Jim", Instant.now(), "MoreMeMore", mlist));
  }

  @JStache(path = "ui/home.mustache")
  public record Home(String name) {}

}
