package org.example.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.ExceptionHandler;
import io.avaje.http.api.Filter;
import io.avaje.http.api.Produces;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpFilter.FilterChain;

@Controller
public class ErrorController {

  @Filter
  void filter(FilterChain chain) {
    // do nothing
    chain.proceed();
  }

  @ExceptionHandler
  String exception(RuntimeException ex) {
    return "Err: " + ex;
  }

  @Produces(statusCode = 501)
  @ExceptionHandler
  HelloDto exceptionCtx(IllegalAccessException ex, Context ctx) {
    return null;
  }
}
