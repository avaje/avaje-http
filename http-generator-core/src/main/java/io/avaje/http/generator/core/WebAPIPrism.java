package io.avaje.http.generator.core;

import java.util.Optional;

import javax.lang.model.element.Element;

public interface WebAPIPrism {

  String value();

  static Optional<WebAPIPrism> getOptionalOn(Element e) {

    return Optional.<WebAPIPrism>empty()
        .or(() -> ControllerPrism.getOptionalOn(e))
        .or(() -> ClientPrism.getOptionalOn(e));
  }
}
