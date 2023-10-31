package io.avaje.http.generator.javalin;

import static io.avaje.http.generator.javalin.JavalinWebMethod.*;
import io.avaje.http.generator.core.CustomWebMethod;
import io.avaje.http.generator.core.WebMethod;

public abstract class AbstractCustomMethodPrism implements CustomWebMethod {

  @Override
  public WebMethod webMethod() {
    if (this instanceof AfterPrism) {
      return AFTER;
    } else if (this instanceof BeforePrism) {
      return BEFORE;
    } else if (this instanceof AfterMatchedPrism) {
      return AFTER_MATCHED;
    } else {
      return BEFORE_MATCHED;
    }
  }
}
