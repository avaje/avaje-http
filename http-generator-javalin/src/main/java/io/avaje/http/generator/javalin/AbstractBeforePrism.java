package io.avaje.http.generator.javalin;

import io.avaje.http.generator.core.CustomWebMethod;
import io.avaje.http.generator.core.WebMethod;

public abstract class AbstractBeforePrism implements CustomWebMethod {

  @Override
  public WebMethod webMethod() {
    return JavalinWebMethod.BEFORE;
  }
}
