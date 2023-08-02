package io.avaje.http.generator.javalin;

import io.avaje.http.generator.core.CustomWebMethod;
import io.avaje.http.generator.core.WebMethod;

public abstract class AbstractCustomMethodPrism implements CustomWebMethod {

  @Override
  public WebMethod webMethod() {
    if (this instanceof AfterPrism) {
      return JavalinWebMethod.AFTER;
    } else {
      return JavalinWebMethod.BEFORE;
    }
  }
}
