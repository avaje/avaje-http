package io.avaje.http.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpenApiSchemaNormalizerTest {

  @Test
  void nullableScalarUsesTypeUnion() {
    final var schema = OpenApiSchemaNormalizer.nullable(new StringSchema());

    assertThat(schema.getType()).isNull();
    assertThat(schema.getTypes()).containsExactly("string", "null");
    assertThat(schema.getNullable()).isNull();
  }

  @Test
  void nullableReferenceUsesAnyOfWrapper() {
    final var reference = new Schema<>().$ref("#/components/schemas/Foo").nullable(true);

    final var schema = OpenApiSchemaNormalizer.nullable(reference);

    assertThat(schema).isNotSameAs(reference);
    assertThat(reference.getNullable()).isNull();
    assertThat(schema.getAnyOf()).hasSize(2);
    assertThat(schema.getAnyOf().get(0).get$ref()).isEqualTo("#/components/schemas/Foo");
    assertThat(schema.getAnyOf().get(1).getType()).isEqualTo("null");
  }

  @Test
  void nullableWrapperIsIdempotent() {
    final var reference = new Schema<>().$ref("#/components/schemas/Foo");

    final var schema = OpenApiSchemaNormalizer.nullable(OpenApiSchemaNormalizer.nullable(reference));

    assertThat(schema.getAnyOf()).hasSize(2);
    assertThat(schema.getAnyOf().get(0).get$ref()).isEqualTo("#/components/schemas/Foo");
    assertThat(schema.getAnyOf().get(1).getType()).isEqualTo("null");
  }

  @Test
  void notNullableRemovesNullFromTypeUnion() {
    final var schema = new Schema<>().types(new LinkedHashSet<>(List.of("string", "null")));

    final var result = OpenApiSchemaNormalizer.notNullable(schema);

    assertThat(result).isSameAs(schema);
    assertThat(schema.getType()).isNull();
    assertThat(schema.getTypes()).containsExactly("string");
    assertThat(schema.getNullable()).isNull();
  }

  @Test
  void notNullableUnwrapsNullableReferenceWrapper() {
    final var reference = new Schema<>().$ref("#/components/schemas/Foo");
    final var wrapper = OpenApiSchemaNormalizer.nullable(reference);

    final var schema = OpenApiSchemaNormalizer.notNullable(wrapper);

    assertThat(schema.get$ref()).isEqualTo("#/components/schemas/Foo");
  }

  @Test
  void legacyExclusiveBoundsConvertToNumericBounds() {
    final var schema = new NumberSchema()
      .minimum(new BigDecimal("1.5"))
      .exclusiveMinimum(true)
      .maximum(new BigDecimal("9.5"))
      .exclusiveMaximum(true);

    assertThat(OpenApiSchemaNormalizer.exclusiveMinimumValue(schema)).isEqualByComparingTo("1.5");
    assertThat(OpenApiSchemaNormalizer.exclusiveMaximumValue(schema)).isEqualByComparingTo("9.5");
    assertThat(OpenApiSchemaNormalizer.omitMinimum(schema)).isTrue();
    assertThat(OpenApiSchemaNormalizer.omitMaximum(schema)).isTrue();
  }

  @Test
  void normalizeTypesKeepsConcreteTypeBeforeNull() {
    final var types = OpenApiSchemaNormalizer.normalizeTypes(
      "string", new LinkedHashSet<>(List.of("null")), true);

    assertThat(types).containsExactly("string", "null");
  }
}
