package io.avaje.http.generator.core.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashSet;
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
}
