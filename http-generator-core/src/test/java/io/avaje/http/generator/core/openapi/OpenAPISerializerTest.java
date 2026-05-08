package io.avaje.http.generator.core.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.media.Schema;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OpenAPISerializerTest {

  @Test
  void serializesSchemaTypesAsTypeArray() throws IllegalAccessException {
    final var schema = new Schema<>().types(new LinkedHashSet<>(Set.of("string", "null")));

    final var json = OpenAPISerializer.serialize(schema);

    assertThat(json).contains("\"type\" : [");
    assertThat(json).contains("\"null\"");
    assertThat(json).doesNotContain("\"types\"");
  }

  @Test
  void serializesSingleSchemaTypeAsScalar() throws IllegalAccessException {
    final var schema = new Schema<>().types(new LinkedHashSet<>(Set.of("string")));

    final var json = OpenAPISerializer.serialize(schema);

    assertThat(json).contains("\"type\" : \"string\"");
    assertThat(json).doesNotContain("\"type\" : [");
    assertThat(json).doesNotContain("\"types\"");
  }

  @Test
  void serializesNumericExclusiveBounds() throws IllegalAccessException {
    final var schema = new Schema<>()
      .type("number")
      .exclusiveMinimumValue(new BigDecimal("1.5"))
      .exclusiveMaximumValue(new BigDecimal("9.5"));

    final var json = OpenAPISerializer.serialize(schema);

    assertThat(json).contains("\"exclusiveMinimum\" : 1.5");
    assertThat(json).contains("\"exclusiveMaximum\" : 9.5");
    assertThat(json).doesNotContain("\"exclusiveMinimumValue\"");
    assertThat(json).doesNotContain("\"exclusiveMaximumValue\"");
  }

  @Test
  void serializesLegacyBooleanExclusiveBoundsAsNumericBounds() throws IllegalAccessException {
    final var schema = new Schema<>()
      .type("integer")
      .minimum(BigDecimal.ONE)
      .exclusiveMinimum(true)
      .maximum(BigDecimal.TEN)
      .exclusiveMaximum(true);

    final var json = OpenAPISerializer.serialize(schema);

    assertThat(json).contains("\"exclusiveMinimum\" : 1");
    assertThat(json).contains("\"exclusiveMaximum\" : 10");
    assertThat(json).doesNotContain("\"minimum\" : 1");
    assertThat(json).doesNotContain("\"maximum\" : 10");
    assertThat(json).doesNotContain("\"exclusiveMinimum\" : true");
    assertThat(json).doesNotContain("\"exclusiveMaximum\" : true");
  }

  @Test
  void serializesBigDecimalAsJsonNumber() throws IllegalAccessException {
    final var json = OpenAPISerializer.serialize(Map.of("value", new BigDecimal("12.34")));

    assertThat(json).isEqualTo("{\"value\" : 12.34}");
  }
}
