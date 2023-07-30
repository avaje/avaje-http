package io.avaje.http.generator.helidon.nima;

import io.avaje.http.generator.core.CustomWebMethod;
import io.avaje.http.generator.core.WebMethod;

public abstract class AbstractFilterPrism implements CustomWebMethod {

  @Override
  public WebMethod webMethod() {

    return HelidonWebMethod.FILTER;
  }
}
