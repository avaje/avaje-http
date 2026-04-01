package io.avaje.http.generator.core.openapi;

import io.avaje.http.generator.prisms.SchemaPrism;
import io.swagger.v3.oas.models.media.Schema;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Predicate;

final class SchemaPrismHelper {

  private static final String DEFAULT_SENTINEL = "##default";

  public static Optional<TypeMirror> implementation(final SchemaPrism schemaPrism) {
    return Optional.ofNullable(schemaPrism.implementation())
      .filter(typeMirror -> !"java.lang.Void".equals(typeMirror.toString()));
  }

  public static void overwriteFromPrism(final Schema<?> schema, final SchemaPrism schemaPrism) {
    Optional.ofNullable(schemaPrism.description())
      .filter(Predicate.not(String::isBlank))
      .filter(Predicate.not(DEFAULT_SENTINEL::equals))
      .ifPresent(schema::description);
    Optional.ofNullable(schemaPrism.name())
      .filter(Predicate.not(String::isBlank))
      .filter(Predicate.not(DEFAULT_SENTINEL::equals))
      .ifPresent(schema::name);
    Optional.ofNullable(schemaPrism.title())
      .filter(Predicate.not(String::isBlank))
      .filter(Predicate.not(DEFAULT_SENTINEL::equals))
      .ifPresent(schema::title);
    Optional.ofNullable(schemaPrism.pattern())
      .filter(Predicate.not(String::isBlank))
      .filter(Predicate.not(DEFAULT_SENTINEL::equals))
      .ifPresent(schema::pattern);
    Optional.ofNullable(schemaPrism.example())
      .filter(Predicate.not(String::isBlank))
      .filter(Predicate.not(DEFAULT_SENTINEL::equals))
      .ifPresent(schema::example);
    Optional.ofNullable(schemaPrism.defaultValue())
      .filter(Predicate.not(String::isBlank))
      .filter(Predicate.not(DEFAULT_SENTINEL::equals))
      .ifPresent(schema::setDefault);

  }
}
