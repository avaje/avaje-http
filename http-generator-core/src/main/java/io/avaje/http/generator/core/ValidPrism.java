package io.avaje.http.generator.core;

import java.util.Optional;

import javax.lang.model.element.Element;

import io.avaje.prism.GeneratePrism;

@GeneratePrism(
    value = io.avaje.validation.constraints.Valid.class,
    name = "AvajeValidPrism",
    superInterfaces = ValidPrism.class)
@GeneratePrism(
  value = javax.validation.Valid.class,
  name = "JavaxValidPrism",
  superInterfaces = ValidPrism.class)
@GeneratePrism(
    value = jakarta.validation.Valid.class,
    name = "JakartaValidPrism",
    superInterfaces = ValidPrism.class)
@GeneratePrism(
    value = io.avaje.http.api.Valid.class,
    name = "HttpValidPrism",
    superInterfaces = ValidPrism.class)
public interface ValidPrism {

  static Optional<ValidPrism> getOptionalOn(Element e) {
    return Optional.<ValidPrism>empty()
        .or(() -> AvajeValidPrism.getOptionalOn(e))
        .or(() -> HttpValidPrism.getOptionalOn(e))
        .or(() -> JakartaValidPrism.getOptionalOn(e))
        .or(() -> JavaxValidPrism.getOptionalOn(e));
  }

  static boolean isPresent(Element e) {
    return AvajeValidPrism.isPresent(e)
        || JakartaValidPrism.isPresent(e)
        || JavaxValidPrism.isPresent(e)
        || HttpValidPrism.isPresent(e);
  }
}
