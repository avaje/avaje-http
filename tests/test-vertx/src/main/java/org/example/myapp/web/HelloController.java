package org.example.myapp.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Header;
import io.avaje.http.api.Produces;
import io.avaje.http.api.QueryParam;

@Controller
public class HelloController {

  @Produces("text/plain")
  @Get("hello")
  String helloWorld() {
    return "hello world";
  }

  @Produces("text/plain")
  @Get("hello/with-params/{id}")
  String withParams(long id, @QueryParam("q") String query, @Header("X-Trace") String traceHeader) {
    return "id=" + id + ";q=" + query + ";trace=" + traceHeader;
  }
}
