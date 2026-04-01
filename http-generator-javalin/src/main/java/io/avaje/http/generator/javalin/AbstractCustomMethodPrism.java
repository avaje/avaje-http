package io.avaje.http.generator.javalin;

import static io.avaje.http.generator.javalin.JavalinWebMethod.*;
import io.avaje.http.generator.core.CustomWebMethod;
import io.avaje.http.generator.core.WebMethod;
import io.avaje.prism.GeneratePrism;

// TODO: Untangle this? Or leave it?
@GeneratePrism(value = io.avaje.http.api.javalin.After.class, superClass = AbstractCustomMethodPrism.class, publicAccess = true)
@GeneratePrism(value = io.avaje.http.api.javalin.Before.class, superClass = AbstractCustomMethodPrism.class, publicAccess = true)
@GeneratePrism(value = io.avaje.http.api.javalin.AfterMatched.class, superClass = AbstractCustomMethodPrism.class, publicAccess = true)
@GeneratePrism(value = io.avaje.http.api.javalin.BeforeMatched.class, superClass = AbstractCustomMethodPrism.class, publicAccess = true)
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
