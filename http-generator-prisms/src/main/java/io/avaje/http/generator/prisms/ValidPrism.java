package io.avaje.http.generator.prisms;

import java.util.Optional;
import java.util.function.Function;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;

import io.avaje.prism.GeneratePrism;

@GeneratePrism(
    value = io.avaje.validation.constraints.Valid.class,
    name = "AvajeValidPrism",
    superInterfaces = ValidPrism.class,
  publicAccess = true)
@GeneratePrism(
    value = javax.validation.Valid.class,
    name = "JavaxValidPrism",
    superInterfaces = ValidPrism.class,
    publicAccess = true)
@GeneratePrism(
    value = jakarta.validation.Valid.class,
    name = "JakartaValidPrism",
    superInterfaces = ValidPrism.class,
  publicAccess = true)
@GeneratePrism(
    value = io.avaje.http.api.Valid.class,
    name = "HttpValidPrism",
    superInterfaces = ValidPrism.class,
  publicAccess = true)
public interface ValidPrism {

  static Optional<ValidPrism> getOptionalOn(Element e) {
    return Optional.<ValidPrism>empty()
        .or(() -> AvajeValidPrism.getOptionalOn(e))
        .or(() -> HttpValidPrism.getOptionalOn(e))
        .or(() -> JakartaValidPrism.getOptionalOn(e))
        .or(() -> JavaxValidPrism.getOptionalOn(e))
        .or(() -> typeUse(e));
  }

  static boolean isPresent(Element e) {
    return AvajeValidPrism.isPresent(e)
        || JakartaValidPrism.isPresent(e)
        || JavaxValidPrism.isPresent(e)
        || HttpValidPrism.isPresent(e)
        || typeUse(e).isPresent();
  }

  static Optional<ValidPrism> typeUse(Element e) {
    if (e instanceof ExecutableElement) {
      ExecutableElement ee = (ExecutableElement) e;
      var returnType = ee.getReturnType();
      if (returnType.getKind() != TypeKind.VOID) {
        return returnType.getAnnotationMirrors().stream()
          .map(m ->
            Optional.<ValidPrism>empty()
              .or(() -> AvajeValidPrism.getOptional(m))
              .or(() -> JakartaValidPrism.getOptional(m))
              .or(() -> JavaxValidPrism.getOptional(m)))
          .filter(Optional::isPresent)
          .findFirst()
          .flatMap(Function.identity());
      }
    }
    return Optional.empty();
  }
}
